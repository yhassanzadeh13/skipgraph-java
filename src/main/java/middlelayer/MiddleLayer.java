package middlelayer;

import lookup.LookupTable;
import model.identifier.Identifier;
import model.identifier.Identity;
import model.identifier.MembershipVector;
import module.logger.Logger;
import module.logger.SkipGraphLogger;
import node.Node;
import node.skipgraph.SearchResult;
import underlay.Underlay;
import underlay.packets.Request;
import underlay.packets.Response;
import underlay.packets.requests.AcquireLockRequest;
import underlay.packets.requests.AnnounceNeighborRequest;
import underlay.packets.requests.FindLadderRequest;
import underlay.packets.requests.GetIdentityRequest;
import underlay.packets.requests.GetLeftNodeRequest;
import underlay.packets.requests.GetRightNodeRequest;
import underlay.packets.requests.ReleaseLockRequest;
import underlay.packets.requests.SearchByIdentifierRequest;
import underlay.packets.requests.SearchByMembershipVectorRecursiveRequest;
import underlay.packets.requests.SearchByMembershipVectorRequest;
import underlay.packets.requests.UpdateLookupTableNeighborRequest;
import underlay.packets.responses.AckResponse;
import underlay.packets.responses.BooleanResponse;
import underlay.packets.responses.IdentityResponse;
import underlay.packets.responses.SearchResultResponse;


/**
 * Represents a mediator between the overlay and the underlay. The requests coming from the underlay
 * are directed to the overlay and the responses emitted by the overlay are returned to the
 * underlay. The requests coming from the overlay are either directed to the underlay or to another
 * local overlay, and the emitted response is returned to the overlay.
 */
public class MiddleLayer {
  /**
   * Max trial denotes the maximum number of send trial attempts before giving up.
   */
  private static final int MAX_TRIAL = 3;
  private final Logger logger;
  private final Underlay underlay;
  private final Node overlay;

  /**
   * Constructor for MiddleLayer.
   *
   * @param underlay underlay instance.
   * @param overlay  Skip node implementation which represents the overlay.
   */
  public MiddleLayer(Underlay underlay, Node overlay) {
    this.underlay = underlay;
    this.overlay = overlay;
    this.logger = SkipGraphLogger.getLoggerForNodeComponent(this.getClass().getName(), overlay.getIdentity().getIdentifier());
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
          this.logger.fatal("could not backoff the send trial attempt", e);
          return null; // technically the return should never executed, since this is a fatal log.
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
    Identity identity;
    SearchResult result;

    // If the overlay is locked, return a response denoting the client to try again later.
    switch (request.type) {
      case SearchByMembershipVector:
        // Check whether the node is available for lookups (i.e., already inserted.)
        if (!overlay.isAvailable()) {
          return new Response(true);
        }
        result = overlay.searchByMembershipVector(((SearchByMembershipVectorRequest) request).targetMembershipVector);
        return new SearchResultResponse(result);
      case SearchByMembershipVectorRecursive:
        // Check whether the node is available for lookups (i.e., already inserted.)
        if (!overlay.isAvailable()) {
          return new Response(true);
        }
        result = overlay.searchByMembershipVector(((SearchByMembershipVectorRecursiveRequest) request).target,
                                                  ((SearchByMembershipVectorRecursiveRequest) request).level);
        return new SearchResultResponse(result);
      case SearchByIdentifier:
        // Check whether the node is available for lookups (i.e., already inserted.)
        if (!overlay.isAvailable()) {
          return new Response(true);
        }
        identity = overlay.searchByIdentifier(((SearchByIdentifierRequest) request).searchByIdentifier);
        return new IdentityResponse(identity);
      case GetIdentity:
        identity = overlay.getIdentity();
        return new IdentityResponse(identity);
      case AcquireLock:
        return new BooleanResponse(overlay.tryAcquire(((AcquireLockRequest) request).requester));
      case ReleaseLock:
        return new BooleanResponse(overlay.unlock(((ReleaseLockRequest) request).owner));
      case UpdateLookupTableNeighbor:
        // TODO: this entire logic must be encapsulated as handle update lookup table neighbor in Overlay (not middlelayer).
        // Can only be invoked when unlocked or by the lock owner.
        if (overlay.isLocked() && !overlay.isLockedBy(request.senderAddress, request.senderPort)) {
          return new Response(true);
        }
        UpdateLookupTableNeighborRequest updateLookupTableNeighborRequest = (UpdateLookupTableNeighborRequest) request;
        if (updateLookupTableNeighborRequest.direction.isLeft()) {
          identity = overlay.updateLeftNode(updateLookupTableNeighborRequest.identity, updateLookupTableNeighborRequest.level);
        } else {
          identity = overlay.updateRightNode(updateLookupTableNeighborRequest.identity, updateLookupTableNeighborRequest.level);
        }
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
        overlay.announceNeighbor(((AnnounceNeighborRequest) request).newNeighbor, ((AnnounceNeighborRequest) request).minLevel);
        return new AckResponse();
      case IsAvailable:
        return new BooleanResponse(overlay.isAvailable());
      default:
        return null;
    }
  }

  /**
   * Searching by membership vector recursively.
   *
   * @param destinationAddress String value representing the destination address.
   * @param port               Integer value representing the port.
   * @param target             Target membership vector of the search.
   * @param level              Integer representing level.
   * @return search result instance.
   */
  public SearchResult searchByMembershipVector(String destinationAddress, int port, MembershipVector target, int level) {
    Request request = new SearchByMembershipVectorRecursiveRequest(target, level);
    // Send the request through the underlay.
    Response response = this.send(destinationAddress, port, request);
    return ((SearchResultResponse) response).result;
  }

  public Identity searchByIdentifier(Identifier identifier, String destinationAddress, int port) {
    return searchByIdentifier(destinationAddress, port, identifier);
  }

  /**
   * Method for searching with identifier.
   *
   * @param destinationAddress String value representing the destination address.
   * @param port               Integer value representing the port.
   * @param targetIdentifier   target identifier.
   * @return skip node identity.
   */
  public Identity searchByIdentifier(String destinationAddress, int port, Identifier targetIdentifier) {
    Request request = new SearchByIdentifierRequest(targetIdentifier);
    // Send the request through the underlay
    Response response = this.send(destinationAddress, port, request);
    return ((IdentityResponse) response).identity;
  }

  /**
   * Method for trying to acquire the lock.
   *
   * @param destinationAddress String value representing the destination address.
   * @param port               Integer value representing the port.
   * @param req                skip node identity.
   * @return boolean value representing whether the lock is acquired or not.
   */
  public boolean tryAcquire(String destinationAddress, int port, Identity req) {
    Request request = new AcquireLockRequest(req);

    Response response = this.send(destinationAddress, port, request);
    return ((BooleanResponse) response).answer;
  }

  /**
   * Method for unlocking the lock.
   *
   * @param destinationAddress String value representing the destination address.
   * @param port               Integer value representing the port.
   * @param owner              owner node.
   * @return boolean value representing if the lock is unlocked or not.
   */
  public boolean unlock(String destinationAddress, int port, Identity owner) {
    Request request = new ReleaseLockRequest(owner);
    Response response = this.send(destinationAddress, port, request);
    return ((BooleanResponse) response).answer;
  }

  /**
   * Method for getting the identity.
   *
   * @param destinationAddress String value representing the destination address.
   * @param port               Integer value representing the port.
   * @return skip node identity.
   */
  public Identity getIdentity(String destinationAddress, int port) {
    Request request = new GetIdentityRequest();
    Response r = send(destinationAddress, port, new GetIdentityRequest());
    return ((IdentityResponse) r).identity;
  }

  public Identity getRightNeighborOf(String destinationAddress, int port, int level) {
    return getRightNeighborOf(true, destinationAddress, port, level);
  }

  /**
   * Method for getting the right node.
   *
   * @param backoff            boolean value for back off.
   * @param destinationAddress String value representing the destination address.
   * @param port               Integer value representing the port.
   * @param level              Integer representing the level
   * @return skip node identity.
   */
  public Identity getRightNeighborOf(boolean backoff, String destinationAddress, int port, int level) {
    // Send the request through the underlay
    GetRightNodeRequest req = new GetRightNodeRequest(level);
    req.backoff = backoff;
    Response r = send(destinationAddress, port, req);
    // If the client has returned a locked response (i.e., has indicated that we should try again),
    // return an invalid skip node identity.
    if (r.locked) {
      return LookupTable.INVALID_NODE;
    }
    return ((IdentityResponse) r).identity;
  }

  public Identity getLeftNeighborOf(String destinationAddress, int port, int level) {
    return getLeftNeighborOf(true, destinationAddress, port, level);
  }

  /**
   * Method for getting the left node.
   *
   * @param backoff            boolean value for back off.
   * @param destinationAddress String value representing the destination address.
   * @param port               Integer value representing the port.
   * @param level              Integer representing the level
   * @return skip node identity.
   */
  public Identity getLeftNeighborOf(boolean backoff, String destinationAddress, int port, int level) {
    // Send the request through the underlay
    GetLeftNodeRequest req = new GetLeftNodeRequest(level);
    req.backoff = backoff;
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
   * @param port               Integer value representing the port.
   * @param level              Integer representing the level.
   * @param direction          Integer representing the direction.
   * @param membershipVector   String representing the target.
   * @return skip node identity.
   */
  public Identity findLadder(String destinationAddress, int port, int level, int direction, MembershipVector membershipVector) {
    Request request = new FindLadderRequest(level, direction, membershipVector);
    // Send the request through the underlay
    Response r = send(destinationAddress, port, request);
    return ((IdentityResponse) r).identity;
  }

  /**
   * Method for announcing a new neighbour.
   *
   * @param destinationAddress String value representing the destination address.
   * @param port               Integer value representing the port.
   * @param newNeighbor        skip node identity of the new neighbour.
   * @param minLevel           Integer representing the minimum level.
   */
  public void announceNeighbor(String destinationAddress, int port, Identity newNeighbor, int minLevel) {
    Request request = new AnnounceNeighborRequest(newNeighbor, minLevel);
    // Send the request through the underlay
    send(destinationAddress, port, request);
  }

  /**
   * Terminates the middlelayer and its underlying network.
   *
   * @return true if the middlelayer stopped successfully. False if there is an error stopping it.
   */
  // TODO: this should be with a timeout.
  public boolean terminate() {
    this.underlay.terminate();
    return true;
  }
}
