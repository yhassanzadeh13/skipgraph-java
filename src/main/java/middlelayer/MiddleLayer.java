package middlelayer;

import java.util.ArrayList;
import java.util.List;
import lookup.LookupTable;
import lookup.TentativeTable;
import skipnode.SearchResult;
import skipnode.SkipNodeIdentity;
import skipnode.SkipNodeInterface;
import underlay.Underlay;
import underlay.packets.Request;
import underlay.packets.Response;
import underlay.packets.requests.AcquireLockRequest;
import underlay.packets.requests.AcquireNeighborsRequest;
import underlay.packets.requests.AnnounceNeighborRequest;
import underlay.packets.requests.FindLadderRequest;
import underlay.packets.requests.GetIdentityRequest;
import underlay.packets.requests.GetLeftLadderRequest;
import underlay.packets.requests.GetLeftNodeRequest;
import underlay.packets.requests.GetRightLadderRequest;
import underlay.packets.requests.GetRightNodeRequest;
import underlay.packets.requests.IncrementRequest;
import underlay.packets.requests.InjectionRequest;
import underlay.packets.requests.IsAvailableRequest;
import underlay.packets.requests.ReleaseLockRequest;
import underlay.packets.requests.SearchByNameIdRecursiveRequest;
import underlay.packets.requests.SearchByNameIdRequest;
import underlay.packets.requests.SearchByNumIdRequest;
import underlay.packets.requests.UpdateLeftNodeRequest;
import underlay.packets.requests.UpdateRightNodeRequest;
import underlay.packets.responses.AckResponse;
import underlay.packets.responses.BooleanResponse;
import underlay.packets.responses.IdentityResponse;
import underlay.packets.responses.SearchResultResponse;
import underlay.packets.responses.TableResponse;

/**
 * Represents a mediator between the overlay and the underlay. The requests coming from the underlay
 * are directed to the overlay and the responses emitted by the overlay are returned to the
 * underlay. The requests coming from the overlay are either directed to the underlay or to another
 * local overlay, and the emitted response is returned to the overlay.
 */
public class MiddleLayer {

  private final Underlay underlay;
  private final SkipNodeInterface masterOverlay;
  private final ArrayList<SkipNodeInterface> overlays;

  /**
   * Constructor for MiddleLayer.
   *
   * @param underlay underlay instance.
   * @param overlay Skip node implementation which represents the overlay.
   */
  public MiddleLayer(Underlay underlay, SkipNodeInterface overlay) {
    this.underlay = underlay;
    this.masterOverlay = overlay;
    this.overlays = new ArrayList<>();
    this.overlays.add(overlay);
  }

  /**
   * Called by the overlay to send requests to the underlay.
   *
   * @param destinationAddress destination address.
   * @param port               destination port.
   * @param request            the request.
   * @return the response emitted by the remote client.
   */
  protected Response send(String destinationAddress, int port, Request request) {
    // Fill out the request's sender information to be used by the remote middle layer.
    request.senderAddress = underlay.getAddress();
    request.senderPort = underlay.getPort();
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
      if (destinationAddress.equals(underlay.getAddress()) && port == underlay.getPort()) {
        // Bounce the request up.
        response = receive(request);
      } else {
        // Or receive it from the remote client.
        response = underlay.sendMessage(destinationAddress, port, request);
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
        result = overlay.searchByNameIdRecursive(((SearchByNameIdRecursiveRequest) request).target,
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
        return new BooleanResponse(overlay.tryAcquire(((AcquireLockRequest) request).requester,
            ((AcquireLockRequest) request).version));
      case ReleaseLock:
        return new BooleanResponse(overlay.unlock(((ReleaseLockRequest) request).owner));
      case UpdateLeftNode:
        // Can only be invoked when unlocked or by the lock owner.
        if (overlay.isLocked() && !overlay.isLockedBy(request.senderAddress, request.senderPort)) {
          return new Response(true);
        }
        identity = overlay.updateLeftNode(((UpdateLeftNodeRequest) request).snId,
            ((UpdateLeftNodeRequest) request).level);
        return new IdentityResponse(identity);
      case UpdateRightNode:
        // Can only be invoked when unlocked or by the lock owner.
        if (overlay.isLocked() && !overlay.isLockedBy(request.senderAddress, request.senderPort)) {
          return new Response(true);
        }
        identity = overlay.updateRightNode(((UpdateRightNodeRequest) request).snId,
            ((UpdateRightNodeRequest) request).level);
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
        identity = overlay.findLadder(((FindLadderRequest) request).level,
            ((FindLadderRequest) request).direction,
            ((FindLadderRequest) request).target);
        return new IdentityResponse(identity);
      case AnnounceNeighbor:
        overlay.announceNeighbor(((AnnounceNeighborRequest) request).newNeighbor,
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
   * @param port Integer representing the port.
   * @param receiverId ID of the receiver.
   * @param nameId name id to be searched.
   * @return Search results from the search.
   */
  public SearchResult searchByNameId(String destinationAddress, int port, int receiverId,
      String nameId) {
    Request request = new SearchByNameIdRequest(nameId);
    request.receiverId = receiverId;
    // Send the request through the underlay
    Response response = this.send(destinationAddress, port, request);
    return ((SearchResultResponse) response).result;
  }

  public SearchResult searchByNameIdRecursive(String destinationAddress, int port, String target,
      int level) {
    return searchByNameIdRecursive(destinationAddress, port, -1, target, level);
  }

  public SearchResult searchByNameIdRecursive(String destinationAddress, int port, int receiverId,
      String target, int level) {
    Request request = new SearchByNameIdRecursiveRequest(target, level);
    request.receiverId = receiverId;
    // Send the request through the underlay.
    Response response = this.send(destinationAddress, port, request);
    return ((SearchResultResponse) response).result;
  }

  public SkipNodeIdentity searchByNumId(String destinationAddress, int port, int numId) {
    return searchByNumId(destinationAddress, port, -1, numId);
  }

  public SkipNodeIdentity searchByNumId(String destinationAddress, int port, int receiverId,
      int numId) {
    Request request = new SearchByNumIdRequest(numId);
    request.receiverId = receiverId;
    // Send the request through the underlay
    Response response = this.send(destinationAddress, port, request);
    return ((IdentityResponse) response).identity;
  }

  public boolean tryAcquire(String destinationAddress, int port, SkipNodeIdentity req,
      int version) {
    return tryAcquire(destinationAddress, port, -1, req, version);
  }

  public boolean tryAcquire(String destinationAddress, int port, int receiverId,
      SkipNodeIdentity req, int version) {
    Request request = new AcquireLockRequest(req, version);
    request.receiverId = receiverId;

    Response response = this.send(destinationAddress, port, request);
    return ((BooleanResponse) response).answer;
  }

  public boolean unlock(String destinationAddress, int port, SkipNodeIdentity owner) {
    return unlock(destinationAddress, port, -1, owner);
  }

  public boolean unlock(String destinationAddress, int port, int receiverId,
      SkipNodeIdentity owner) {
    Request request = new ReleaseLockRequest(owner);
    request.receiverId = receiverId;
    Response response = this.send(destinationAddress, port, request);
    return ((BooleanResponse) response).answer;
  }

  public SkipNodeIdentity updateRightNode(String destinationAddress, int port,
      SkipNodeIdentity snId, int level) {
    return updateRightNode(destinationAddress, port, -1, snId, level);
  }

  public SkipNodeIdentity updateRightNode(String destinationAddress, int port, int receiverId,
      SkipNodeIdentity snId, int level) {
    Request request = new UpdateRightNodeRequest(level, snId);
    request.receiverId = receiverId;
    // Send the request through the underlay
    Response response = this.send(destinationAddress, port, request);
    return ((IdentityResponse) response).identity;

  }

  public SkipNodeIdentity updateLeftNode(String destinationAddress, int port, SkipNodeIdentity snId,
      int level) {
    return updateLeftNode(destinationAddress, port, -1, snId, level);
  }

  public SkipNodeIdentity updateLeftNode(String destinationAddress, int port, int receiverId,
      SkipNodeIdentity snId, int level) {
    Request request = new UpdateLeftNodeRequest(level, snId);
    request.receiverId = receiverId;
    // Send the request through the underlay
    Response response = this.send(destinationAddress, port, request);
    return ((IdentityResponse) response).identity;
  }

  public SkipNodeIdentity getIdentity(String destinationAddress, int port) {
    return getIdentity(destinationAddress, port, -1);
  }

  public SkipNodeIdentity getIdentity(String destinationAddress, int port, int receiverId) {
    Request request = new GetIdentityRequest();
    request.receiverId = receiverId;
    Response r = send(destinationAddress, port, new GetIdentityRequest());
    return ((IdentityResponse) r).identity;
  }

  public SkipNodeIdentity getLeftNode(String destinationAddress, int port, int level) {
    return getLeftNode(true, destinationAddress, port, -1, level);
  }

  public SkipNodeIdentity getLeftNode(String destinationAddress, int port, int receiverId,
      int level) {
    return getLeftNode(true, destinationAddress, port, receiverId, level);
  }

  public SkipNodeIdentity getRightNode(String destinationAddress, int port, int level) {
    return getRightNode(true, destinationAddress, port, -1, level);
  }

  public SkipNodeIdentity getRightNode(String destinationAddress, int port, int receiverId,
      int level) {
    return getRightNode(true, destinationAddress, port, receiverId, level);
  }

  public SkipNodeIdentity getLeftNode(boolean backoff, String destinationAddress, int port,
      int receiverId, int level) {
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

  public SkipNodeIdentity getRightNode(boolean backoff, String destinationAddress, int port,
      int receiverId, int level) {
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

  public TentativeTable acquireNeighbors(String destinationAddress, int port,
      SkipNodeIdentity newNodeId, int level) {
    return acquireNeighbors(destinationAddress, port, -1, newNodeId, level);
  }

  public TentativeTable acquireNeighbors(String destinationAddress, int port, int receiverId,
      SkipNodeIdentity newNodeId, int level) {
    Request request = new AcquireNeighborsRequest(newNodeId, level);
    request.receiverId = receiverId;
    // Send the request through the underlay
    Response r = send(destinationAddress, port, request);
    return ((TableResponse) r).table;
  }

  public SkipNodeIdentity findLadder(String destinationAddress, int port, int level, int direction,
      String target) {
    return findLadder(destinationAddress, port, -1, level, direction, target);
  }

  public SkipNodeIdentity findLadder(String destinationAddress, int port, int receiverId, int level,
      int direction, String target) {
    Request request = new FindLadderRequest(level, direction, target);
    request.receiverId = receiverId;
    // Send the request through the underlay
    Response r = send(destinationAddress, port, request);
    return ((IdentityResponse) r).identity;
  }

  public void announceNeighbor(String destinationAddress, int port, SkipNodeIdentity newNeighbor,
      int minLevel) {
    announceNeighbor(destinationAddress, port, -1, newNeighbor, minLevel);
  }

  public void announceNeighbor(String destinationAddress, int port, int receiverId,
      SkipNodeIdentity newNeighbor, int minLevel) {
    Request request = new AnnounceNeighborRequest(newNeighbor, minLevel);
    request.receiverId = receiverId;
    // Send the request through the underlay
    send(destinationAddress, port, request);
  }

  public boolean isAvailable(String destinationAddress, int port) {
    return isAvailable(destinationAddress, port, -1);
  }

  public boolean isAvailable(String destinationAddress, int port, int receiverId) {
    Request request = new IsAvailableRequest();
    request.receiverId = receiverId;
    Response r = send(destinationAddress, port, request);
    return ((BooleanResponse) r).answer;
  }

  public SkipNodeIdentity getLeftLadder(String destinationAddress, int port, int level,
      String nameId) {
    return getLeftLadder(destinationAddress, port, -1, level, nameId);
  }

  public SkipNodeIdentity getLeftLadder(String destinationAddress, int port, int receiverId,
      int level, String nameId) {
    Request request = new GetLeftLadderRequest(level, nameId);
    request.receiverId = receiverId;
    // Send the request through the underlay
    Response r = send(destinationAddress, port, request);
    return ((IdentityResponse) r).identity;
  }

  public SkipNodeIdentity getRightLadder(String destinationAddress, int port, int level,
      String nameId) {
    return getRightLadder(destinationAddress, port, -1, level, nameId);
  }

  public SkipNodeIdentity getRightLadder(String destinationAddress, int port, int receiverId,
      int level, String nameId) {
    Request request = new GetRightLadderRequest(level, nameId);
    request.receiverId = receiverId;
    // Send the request through the underlay
    Response r = send(destinationAddress, port, request);
    return ((IdentityResponse) r).identity;
  }

  public SkipNodeIdentity increment(String destinationAddress, int port, SkipNodeIdentity snId,
      int level) {
    return increment(destinationAddress, port, -1, snId, level);
  }

  public SkipNodeIdentity increment(String destinationAddress, int port, int receiverId,
      SkipNodeIdentity snId, int level) {
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

  public boolean inject(String destinationAddress, int port, int receiverId,
      List<SkipNodeIdentity> snIds) {
    Request request = new InjectionRequest(snIds);
    request.receiverId = receiverId;
    // Send the request through the underlay
    Response response = send(destinationAddress, port, request);
    if (response == null) {
      System.exit(1);
    }
    return ((BooleanResponse) response).answer;
  }

}
