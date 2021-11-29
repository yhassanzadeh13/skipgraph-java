package network;

import java.util.ArrayList;
import java.util.List;

import lookup.LookupTable;
import model.Address;
import network.underlay.packets.Request;
import network.underlay.packets.Response;
import network.underlay.packets.requests.*;
import network.underlay.packets.responses.AckResponse;
import network.underlay.packets.responses.BooleanResponse;
import network.underlay.packets.responses.IdentityResponse;
import network.underlay.packets.responses.SearchResultResponse;
import skipnode.SearchResult;
import skipnode.SkipNodeIdentity;
import skipnode.SkipNodeInterface;


/**
 * Represents a mediator between the overlay and the underlay. The requests coming from the underlay
 * are directed to the overlay and the responses emitted by the overlay are returned to the
 * underlay. The requests coming from the overlay are either directed to the underlay or to another
 * local overlay, and the emitted response is returned to the overlay.
 */
public class Network {

  private final Underlay underlay;
  private final SkipNodeInterface masterOverlay;
  private final ArrayList<SkipNodeInterface> overlays;

  /**
   * Constructor for MiddleLayer.
   *
   * @param underlay underlay instance.
   * @param overlay  Skip node implementation which represents the overlay.
   */
  public Network(Underlay underlay, SkipNodeInterface overlay) {
    this.underlay = underlay;
    this.masterOverlay = overlay;
    this.overlays = new ArrayList<>();
    this.overlays.add(overlay);
  }

  /**
   * Called by the overlay to send requests to the underlay.
   *
   * @param destinationAddress destination address.
   * @param request            the request.
   * @return the response emitted by the remote client.
   */
  protected Response send(Address destinationAddress, Request request) {
    // Fill out the request's sender information to be used by the remote middle layer.
    request.originAddress = underlay.getAddress();

    Response response = null;
    int trial = 0;
    do {
      trial++;
      // Backoff.
      if (trial > 1) {
        int sleepTime = (int) (Math.random() * 2000);
        try {
          Thread.sleep(sleepTime);
        } catch (InterruptedException e) {
          System.err.println("[MiddleLayer.send] Could not back off.");
          e.printStackTrace();
        }
      }
      // Check if the destination address == address of this node.
      if (destinationAddress.equals(underlay.getAddress())) {
        // Bounce the request up.
        response = receive(request);
      } else {
        // Or receive it from the remote client.
        response = underlay.sendMessage(destinationAddress.getIp(), destinationAddress.getPort(), request);
      }
    } while (request.backoff && response.locked);

    return response;
  }

  /**
   * Called by the underlay to collect the response from the overlay.
   *
   * @return response emitted by the overlay.
   */
  public Response receive(Request request) {
    SkipNodeIdentity identity;
    SearchResult result;
    SkipNodeInterface overlay =
        request.receiverId == -1 ? masterOverlay : getById(request.receiverId);
    // Invalid ID
    if (overlay == null) {
      return null;
    }
    // If the overlay is locked, return a response denoting the client to try again later.
    switch (request.type) {
      case SearchByNameId:
        // Check whether the node is available for lookups (i.e., already inserted.)
        if (!overlay.isAvailable()) {
          return new Response(true);
        }
        result = overlay.searchByNameId(((SearchByNameIdRequest) request).targetNameId);
        return new SearchResultResponse(result);
      case SearchByNameIDRecursive:
        // Check whether the node is available for lookups (i.e., already inserted.)
        if (!overlay.isAvailable()) {
          return new Response(true);
        }
        result =
            overlay.searchByNameIdRecursive(
                ((SearchByNameIdRecursiveRequest) request).target,
                ((SearchByNameIdRecursiveRequest) request).level);
        return new SearchResultResponse(result);
      case SearchByNumId:
        // Check whether the node is available for lookups (i.e., already inserted.)
        if (!overlay.isAvailable()) {
          return new Response(true);
        }
        identity = overlay.searchByNumId(((SearchByNumIdRequest) request).targetNumId);
        return new IdentityResponse(identity);
      case GetIdentity:
        identity = overlay.getIdentity();
        return new IdentityResponse(identity);
      case AcquireLock:
        return new BooleanResponse(
            overlay.tryAcquire(
                ((AcquireLockRequest) request).requester, ((AcquireLockRequest) request).version));
      case ReleaseLock:
        return new BooleanResponse(overlay.unlock(((ReleaseLockRequest) request).owner));
      case UpdateLeftNode:
        // Can only be invoked when unlocked or by the lock owner.
        if (overlay.isLocked() && !overlay.isLockedBy(request.senderAddress, request.senderPort)) {
          return new Response(true);
        }
        identity =
            overlay.updateLeftNode(
                ((UpdateLeftNodeRequest) request).snId, ((UpdateLeftNodeRequest) request).level);
        return new IdentityResponse(identity);
      case UpdateRightNode:
        // Can only be invoked when unlocked or by the lock owner.
        if (overlay.isLocked() && !overlay.isLockedBy(request.senderAddress, request.senderPort)) {
          return new Response(true);
        }
        identity =
            overlay.updateRightNode(
                ((UpdateRightNodeRequest) request).snId, ((UpdateRightNodeRequest) request).level);
        return new IdentityResponse(identity);
      case GetRightNode:
        // Can only be invoked when unlocked or by the lock owner.
        if (overlay.isLocked() && !overlay.isLockedBy(request.senderAddress, request.senderPort)) {
          return new Response(true);
        }
        identity = overlay.getRightNode(((GetRightNodeRequest) request).level);
        return new IdentityResponse(identity);
      case GetLeftNode:
        // Can only be invoked when unlocked or by the lock owner.
        if (overlay.isLocked() && !overlay.isLockedBy(request.senderAddress, request.senderPort)) {
          return new Response(true);
        }
        identity = overlay.getLeftNode(((GetLeftNodeRequest) request).level);
        return new IdentityResponse(identity);
      case FindLadder:
        // Can only be invoked when unlocked or by the lock owner.
        if (overlay.isLocked() && !overlay.isLockedBy(request.senderAddress, request.senderPort)) {
          return new Response(true);
        }
        identity =
            overlay.findLadder(
                ((FindLadderRequest) request).level,
                ((FindLadderRequest) request).direction,
                ((FindLadderRequest) request).target);
        return new IdentityResponse(identity);
      case AnnounceNeighbor:
        overlay.announceNeighbor(
            ((AnnounceNeighborRequest) request).newNeighbor,
            ((AnnounceNeighborRequest) request).minLevel);
        return new AckResponse();
      case IsAvailable:
        return new BooleanResponse(overlay.isAvailable());
      default:
        return null;
    }
  }

  /**
   * Adds a data node to the list of overlays of the middle layer Inserts the node into the Skip
   * Graph.
   *
   * @param node skip node instance.
   */
  public void insertDataNode(SkipNodeInterface node) {
    overlays.add(node);
    node.setMiddleLayer(this);
    node.insert(node.getIdentity().getAddress(), node.getIdentity().getPort());
  }

  private SkipNodeInterface getById(int id) {
    for (SkipNodeInterface overlay : this.overlays) {
      if (overlay.getIdentity().getNumId() == id) {
        return overlay;
      }
    }
    return null;
  }

  /*
  Implemented methods.
  These are the methods that the Overlay will use to send messages using the middle layer
  TODO: Think about whether we should implement a wrapper class
   to handle this similarly to how RMI returns a callable object
  Possible usage then: dial(address) would return an object that handles
  all the communication to the middle layer and can abstract away all the details,
  allowing for it to be used as if it was simply available locally.
   */

  public SearchResult searchByNameId(String destinationAddress, int port, String nameId) {
    return searchByNameId(destinationAddress, port, -1, nameId);
  }

  /**
   * Method for searching by the name id.
   *
   * @param destinationAddress String representing the destination address.
   * @param port               Integer representing the port.
   * @param receiverId         ID of the receiver.
   * @param nameId             name id to be searched.
   * @return Search results from the search.
   */
  public SearchResult searchByNameId(
      String destinationAddress, int port, int receiverId, String nameId) {
    Request request = new SearchByNameIdRequest(nameId);
    request.receiverId = receiverId;
    // Send the request through the underlay
    Response response = this.send(destinationAddress, port, request);
    return ((SearchResultResponse) response).result;
  }

  public SearchResult searchByNameIdRecursive(Address address, String target, int level) {
    return searchByNameIdRecursive(destinationAddress, port, -1, target, level);
  }

  /**
   * Method for searching by name id recursively.
   *
   * @param destinationAddress String value representing the destination address.
   * @param port               Integer value representing the port.
   * @param receiverId         receiver id.
   * @param target             String value representing the target.
   * @param level              Integer representing level.
   * @return search result instance.
   */
  public SearchResult searchByNameIdRecursive(Address address, int receiverId, String target, int level) {
    Request request = new SearchByNameIdRecursiveRequest(target, level);
    request.receiverId = receiverId;
    // Send the request through the underlay.
    Response response = this.send(address, request);
    return ((SearchResultResponse) response).result;
  }

  public SkipNodeIdentity searchByNumId(Address destinationAddress, int numId) {
    return searchByNumId(destinationAddress, -1, numId);
  }

  /**
   * Method for searching with numerical id.
   *
   * @param destinationAddress String value representing the destination address.
   * @param port               Integer value representing the port.
   * @param receiverId         receiver id.
   * @param numId              target numerical id.
   * @return skip node identity.
   */
  public SkipNodeIdentity searchByNumId(Address address, int receiverId, int numId) {
    Request request = new SearchByNumIdRequest(numId);
    request.receiverId = receiverId;
    // Send the request through the underlay
    Response response = this.send(destinationAddress, port, request);
    return ((IdentityResponse) response).identity;
  }

  public boolean tryAcquire(Address address, SkipNodeIdentity req, int version) {
    return tryAcquire(destinationAddress, port, -1, req, version);
  }

  /**
   * Method for trying to acquire the lock.
   *
   * @param destinationAddress String value representing the destination address.
   * @param port               Integer value representing the port.
   * @param receiverId         receiver id.
   * @param req                skip node identity.
   * @param version            Integer representing the version.
   * @return boolean value representing whether the lock is acquired or not.
   */
  public boolean tryAcquire(Address address, int receiverId, SkipNodeIdentity req, int version) {
    Request request = new AcquireLockRequest(req, version);
    request.receiverId = receiverId;

    Response response = this.send(destinationAddress, port, request);
    return ((BooleanResponse) response).answer;
  }

  public boolean unlock(Address address, int port, SkipNodeIdentity owner) {
    return unlock(destinationAddress, port, -1, owner);
  }

  /**
   * Method for unlocking the lock.
   *
   * @param destinationAddress String value representing the destination address.
   * @param port               Integer value representing the port.
   * @param receiverId         receiver id.
   * @param owner              owner node.
   * @return boolean value representing if the lock is unlocked or not.
   */
  public boolean unlock(
      String destinationAddress, int port, int receiverId, SkipNodeIdentity owner) {
    Request request = new ReleaseLockRequest(owner);
    request.receiverId = receiverId;
    Response response = this.send(destinationAddress, port, request);
    return ((BooleanResponse) response).answer;
  }

  public SkipNodeIdentity updateRightNode(
      String destinationAddress, int port, SkipNodeIdentity snId, int level) {
    return updateRightNode(destinationAddress, port, -1, snId, level);
  }

  /**
   * Method for updating the right node.
   *
   * @param destinationAddress String value representing the destination address.
   * @param port               Integer value representing the port.
   * @param receiverId         receiver id.
   * @param snId               skip node identity.
   * @param level              Integer representing the level.
   * @return skip node identity.
   */
  public SkipNodeIdentity updateRightNode(
      String destinationAddress, int port, int receiverId, SkipNodeIdentity snId, int level) {
    Request request = new UpdateRightNodeRequest(level, snId);
    request.receiverId = receiverId;
    // Send the request through the underlay
    Response response = this.send(destinationAddress, port, request);
    return ((IdentityResponse) response).identity;
  }

  public SkipNodeIdentity updateLeftNode(
      String destinationAddress, int port, SkipNodeIdentity snId, int level) {
    return updateLeftNode(destinationAddress, port, -1, snId, level);
  }

  /**
   * Method for updating the left node.
   *
   * @param destinationAddress String value representing the destination address.
   * @param port               Integer value representing the port.
   * @param receiverId         receiver id.
   * @param snId               skip node identity.
   * @param level              Integer representing the level.
   * @return skip node identity.
   */
  public SkipNodeIdentity updateLeftNode(Address address, int receiverId, SkipNodeIdentity snId, int level) {
    Request request = new UpdateLeftNodeRequest(level, snId);
    request.receiverId = receiverId;
    // Send the request through the underlay
    Response response = this.send(destinationAddress, port, request);
    return ((IdentityResponse) response).identity;
  }

  public SkipNodeIdentity getIdentity(Address address, int port) {
    return getIdentity(destinationAddress, port, -1);
  }

  /**
   * Method for getting the identity.
   *
   * @param destinationAddress String value representing the destination address.
   * @param port               Integer value representing the port.
   * @param receiverId         receiver id.
   * @return skip node identity.
   */
  public SkipNodeIdentity getIdentity(String destinationAddress, int port, int receiverId) {
    Request request = new GetIdentityRequest();
    request.receiverId = receiverId;
    Response r = send(destinationAddress, port, new GetIdentityRequest());
    return ((IdentityResponse) r).identity;
  }

  public SkipNodeIdentity getRightNode(Address destinationAddress, int level) {
    return getRightNode(destinationAddress, port, -1, level, true);
  }

  public SkipNodeIdentity getRightNode(Address destinationAddress, int receiverId, int level) {
    return getRightNode(destinationAddress, receiverId, level, true);
  }

  /**
   * Method for getting the right node.
   *
   * @param backoff            boolean value for back off.
   * @param destinationAddress String value representing the destination address.
   * @param port               Integer value representing the port.
   * @param receiverId         receiver id.
   * @param level              Integer representing the level
   * @return skip node identity.
   */
  public SkipNodeIdentity getRightNode(Address address, int receiverId, int level, boolean backoff) {
    // Send the request through the underlay
    GetRightNodeRequest req = new GetRightNodeRequest(level);
    req.backoff = backoff;
    req.receiverId = receiverId;
    Response r = send(destinationAddress, port, req);
    // If the client has returned a locked response (i.e., has indicated that we should try again),
    // return an invalid skip node identity.
    if (r.locked) {
      return LookupTable.INVALID_NODE;
    }
    return ((IdentityResponse) r).identity;
  }

  public SkipNodeIdentity getLeftNode(Address destinationAddress, int level) {
    return getLeftNode(true, destinationAddress, port, -1, level);
  }

  public SkipNodeIdentity getLeftNode(Address destinationAddress, int receiverId, int level) {
    return getLeftNode(true, destinationAddress, port, receiverId, level);
  }

  /**
   * Method for getting the left node.
   *
   * @param backoff            boolean value for back off.
   * @param destinationAddress String value representing the destination address.
   * @param port               Integer value representing the port.
   * @param receiverId         receiver id.
   * @param level              Integer representing the level
   * @return skip node identity.
   */
  public SkipNodeIdentity getLeftNode(boolean backoff, Address address, int receiverId, int level) {
    // Send the request through the underlay
    GetLeftNodeRequest req = new GetLeftNodeRequest(level);
    req.backoff = backoff;
    req.receiverId = receiverId;
    Response r = send(destinationAddress, port, req);
    // If the client has returned a locked response (i.e., has indicated that we should try again),
    // return an invalid skip node identity.
    if (r.locked) {
      return LookupTable.INVALID_NODE;
    }
    return ((IdentityResponse) r).identity;
  }

  /**
   * Method for finding a ladder.
   *
   * @param destinationAddress String value representing the destination address.
   * @param receiverId         receiver id.
   * @param level              Integer representing the level.
   * @param direction          Integer representing the direction.
   * @param target             String representing the target.
   * @return skip node identity.
   */
  public SkipNodeIdentity findLadder(
      Address destinationAddress,
      int receiverId,
      int level,
      int direction,
      String target) {
    Request request = new FindLadderRequest(level, direction, target);
    request.receiverId = receiverId;
    // Send the request through the underlay
    Response r = send(destinationAddress, port, request);
    return ((IdentityResponse) r).identity;
  }

  public void announceNeighbor(Address address, SkipNodeIdentity newNeighbor, int minLevel) {
    announceNeighbor(destinationAddress, port, -1, newNeighbor, minLevel);
  }

  /**
   * Method for announcing a new neighbour.
   *
   * @param destinationAddress String value representing the destination address.
   * @param port               Integer value representing the port.
   * @param receiverId         receiver id.
   * @param newNeighbor        skip node identity of the new neighbour.
   * @param minLevel           Integer representing the minimum level.
   */
  public void announceNeighbor(
      Address address,
      int receiverId,
      SkipNodeIdentity newNeighbor,
      int minLevel) {
    Request request = new AnnounceNeighborRequest(newNeighbor, minLevel);
    request.receiverId = receiverId;
    // Send the request through the underlay
    send(destinationAddress, port, request);
  }

  public boolean isAvailable(String destinationAddress, int port) {
    return isAvailable(destinationAddress, port, -1);
  }

  /**
   * Method for checking if node is available or not.
   *
   * @param destinationAddress String value representing the destination address.
   * @param port               Integer value representing the port.
   * @param receiverId         receiver id.
   * @return boolean representing if node is available or not.
   */
  public boolean isAvailable(String destinationAddress, int port, int receiverId) {
    Request request = new IsAvailableRequest();
    request.receiverId = receiverId;
    Response r = send(destinationAddress, port, request);
    return ((BooleanResponse) r).answer;
  }

  public SkipNodeIdentity getLeftLadder(
      String destinationAddress, int port, int level, String nameId) {
    return getLeftLadder(destinationAddress, port, -1, level, nameId);
  }

  /**
   * Method for getting the left ladder.
   *
   * @param destinationAddress String value representing the destination address.
   * @param port               Integer value representing the port.
   * @param receiverId         receiver id.
   * @param level              Integer representing the level.
   * @param nameId             String representing the name id of the node.
   * @return skip node identity.
   */
  public SkipNodeIdentity getLeftLadder(
      String destinationAddress, int port, int receiverId, int level, String nameId) {
    Request request = new GetLeftLadderRequest(level, nameId);
    request.receiverId = receiverId;
    // Send the request through the underlay
    Response r = send(destinationAddress, port, request);
    return ((IdentityResponse) r).identity;
  }

  public SkipNodeIdentity getRightLadder(
      String destinationAddress, int port, int level, String nameId) {
    return getRightLadder(destinationAddress, port, -1, level, nameId);
  }

  /**
   * Method for getting the right ladder.
   *
   * @param destinationAddress String value representing the destination address.
   * @param port               Integer value representing the port.
   * @param receiverId         receiver id.
   * @param level              Integer representing the level.
   * @param nameId             String representing the name id of the node.
   * @return skip node identity.
   */
  public SkipNodeIdentity getRightLadder(
      String destinationAddress, int port, int receiverId, int level, String nameId) {
    Request request = new GetRightLadderRequest(level, nameId);
    request.receiverId = receiverId;
    // Send the request through the underlay
    Response r = send(destinationAddress, port, request);
    return ((IdentityResponse) r).identity;
  }

  public SkipNodeIdentity increment(
      String destinationAddress, int port, SkipNodeIdentity snId, int level) {
    return increment(destinationAddress, port, -1, snId, level);
  }

  /**
   * Method for increment.
   *
   * @param destinationAddress String value representing the destination address.
   * @param port               Integer value representing the port.
   * @param receiverId         receiver id.
   * @param snId               skip node identity.
   * @param level              Integer representing the level.
   * @return skip node identity.
   */
  public SkipNodeIdentity increment(
      String destinationAddress, int port, int receiverId, SkipNodeIdentity snId, int level) {
    Request request = new IncrementRequest(level, snId);
    request.receiverId = receiverId;
    // Send the request through the underlay
    try {
      Thread.sleep(10000);
    } catch (Exception e) {
      e.printStackTrace();
    }
    Response response = send(destinationAddress, port, request);
    if (response == null) {
      System.exit(1);
    }
    return ((IdentityResponse) response).identity;
  }

  public boolean inject(String destinationAddress, int port, List<SkipNodeIdentity> snIds) {
    return inject(destinationAddress, port, -1, snIds);
  }

  /**
   * Method for injection.
   *
   * @param destinationAddress String value representing the destination address.
   * @param port               Integer value representing the port.
   * @param receiverId         receiver id.
   * @param snIds              list of skip node identities for injection.
   * @return boolean value representing if injection succeeded or not.
   */
  public boolean inject(
      String destinationAddress, int port, int receiverId, List<SkipNodeIdentity> snIds) {
    Request request = new InjectionRequest(snIds);
    request.receiverId = receiverId;
    // Send the request through the underlay
    Response response = send(destinationAddress, port, request);
    if (response == null) {
      System.exit(1);
    }
    return ((BooleanResponse) response).answer;
  }

  /**
   * Terminates the middlelayer and its underlying network.
   *
   * @return true if the middlelayer stopped successfully. False if there is an error stopping it.
   */
  public boolean terminate() {
    this.underlay.terminate();
    return true;
  }
}