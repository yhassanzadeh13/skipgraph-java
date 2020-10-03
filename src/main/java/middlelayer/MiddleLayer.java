package middlelayer;
import lookup.LookupTable;
import lookup.TentativeTable;
import skipnode.SearchResult;
import skipnode.SkipNodeIdentity;
import skipnode.SkipNodeInterface;
import underlay.Underlay;
import underlay.packets.*;
import underlay.packets.requests.*;
import underlay.packets.responses.*;

import java.util.List;

/**
 * Represents a mediator between the overlay and the underlay. The requests coming from the underlay are directed
 * to the overlay and the responses emitted by the overlay are returned to the underlay. The requests coming from
 * the overlay are either directed to the underlay or to another local overlay, and the emitted response is returned
 * to the overlay.
 */
public class MiddleLayer {

    private final Underlay underlay;
    private final SkipNodeInterface overlay;

    public MiddleLayer(Underlay underlay, SkipNodeInterface overlay) {
        this.underlay = underlay;
        this.overlay = overlay;
    }

    /**
     * Called by the overlay to send requests to the underlay.
     * @param destinationAddress destination address.
     * @param port destination port.
     * @param request the request.
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
            if(trial > 1) {
                int sleepTime = (int) (Math.random() * 2000);
                try {
//                    System.out.println("[MiddleLayer.send] Backing off " + trial + " for " + sleepTime + " ms while sending " + request
//                            + " from " + overlay.getIdentity().getNumID() + " to " + destinationAddress + ":" + port + ".");
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
        } while(request.backoff && response.locked);

        return response;
    }

    /**
     * Called by the underlay to collect the response from the overlay.
     * @return response emitted by the overlay.
     */
    public Response receive(Request request) {
        SkipNodeIdentity identity;
        SearchResult result;
        // If the overlay is locked, return a response denoting the client to try again later.
        switch (request.type) {
            case SearchByNameID:
                // Check whether the node is available for lookups (i.e., already inserted.)
                if(!overlay.isAvailable()) return new Response(true);
                result = overlay.searchByNameID(((SearchByNameIDRequest) request).targetNameID);
                return new SearchResultResponse(result);
            case SearchByNameIDRecursive:
                // Check whether the node is available for lookups (i.e., already inserted.)
                if(!overlay.isAvailable()) return new Response(true);
                result = overlay.searchByNameIDRecursive(((SearchByNameIDRecursiveRequest) request).target,
                        ((SearchByNameIDRecursiveRequest) request).level);
                return new SearchResultResponse(result);
            case SearchByNumID:
                // Check whether the node is available for lookups (i.e., already inserted.)
                if(!overlay.isAvailable()) return new Response(true);
                identity = overlay.searchByNumID(((SearchByNumIDRequest) request).targetNumID);
                return new IdentityResponse(identity);
            case GetIdentity:
                identity = overlay.getIdentity();
                return new IdentityResponse(identity);
            case AcquireLock:
                return new BooleanResponse(overlay.tryAcquire(((AcquireLockRequest) request).requester, ((AcquireLockRequest) request).version));
            case ReleaseLock:
                return new BooleanResponse(overlay.unlock(((ReleaseLockRequest) request).owner));
            case UpdateLeftNode:
                // Can only be invoked when unlocked or by the lock owner.
                if(overlay.isLocked() && !overlay.isLockedBy(request.senderAddress, request.senderPort))
                    return new Response(true);
                identity = overlay.updateLeftNode(((UpdateLeftNodeRequest) request).snId, ((UpdateLeftNodeRequest) request).level);
                return new IdentityResponse(identity);
            case UpdateRightNode:
                // Can only be invoked when unlocked or by the lock owner.
                if(overlay.isLocked() && !overlay.isLockedBy(request.senderAddress, request.senderPort))
                    return new Response(true);
                identity = overlay.updateRightNode(((UpdateRightNodeRequest) request).snId, ((UpdateRightNodeRequest) request).level);
                return new IdentityResponse(identity);
            case GetRightNode:
                // Can only be invoked when unlocked or by the lock owner.
                if(overlay.isLocked() && !overlay.isLockedBy(request.senderAddress, request.senderPort))
                    return new Response(true);
                identity = overlay.getRightNode(((GetRightNodeRequest) request).level);
                return new IdentityResponse(identity);
            case GetLeftNode:
                // Can only be invoked when unlocked or by the lock owner.
                if(overlay.isLocked() && !overlay.isLockedBy(request.senderAddress, request.senderPort))
                    return new Response(true);
                identity = overlay.getLeftNode(((GetLeftNodeRequest) request).level);
                return new IdentityResponse(identity);
            case FindLadder:
                // Can only be invoked when unlocked or by the lock owner.
                if(overlay.isLocked() && !overlay.isLockedBy(request.senderAddress, request.senderPort))
                    return new Response(true);
                identity = overlay.findLadder(((FindLadderRequest) request).level, ((FindLadderRequest) request).direction,
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

    /*
    Implemented methods.
    These are the methods that the Overlay will use to send messages using the middle layer
    TODO: Think about whether we should implement a wrapper class to handle this similarly to how RMI returns a callable object
    Possible usage then: dial(address) would return an object that handles all the communication to the middle layer
    and can abstract away all the details, allowing for it to be used as if it was simply available locally.
     */

    public SearchResult searchByNameID(String destinationAddress, int port, String nameID) {
        // Send the request through the underlay
        Response response = this.send(destinationAddress, port, new SearchByNameIDRequest(nameID));
        return ((SearchResultResponse) response).result;
    }

    public SearchResult searchByNameIDRecursive(String destinationAddress, int port, String target, int level) {
        // Send the request through the underlay.
        Response response = this.send(destinationAddress, port, new SearchByNameIDRecursiveRequest(target, level));
        return ((SearchResultResponse) response).result;
    }

    public SkipNodeIdentity searchByNumID(String destinationAddress, int port, int numID) {
        // Send the request through the underlay
        Response response = this.send(destinationAddress, port, new SearchByNumIDRequest(numID));
        return ((IdentityResponse) response).identity;
    }

    public boolean tryAcquire(String destinationAddress, int port, SkipNodeIdentity req, int version) {
        Response response = this.send(destinationAddress, port, new AcquireLockRequest(req, version));
        return ((BooleanResponse) response).answer;
    }

    public boolean unlock(String destinationAddress, int port, SkipNodeIdentity owner) {
        Response response = this.send(destinationAddress, port, new ReleaseLockRequest(owner));
        return ((BooleanResponse) response).answer;
    }

    public SkipNodeIdentity updateRightNode(String destinationAddress, int port, SkipNodeIdentity snId, int level) {
        // Send the request through the underlay
        Response response = this.send(destinationAddress, port, new UpdateRightNodeRequest(level, snId));
        return ((IdentityResponse) response).identity;

    }

    public SkipNodeIdentity updateLeftNode(String destinationAddress, int port, SkipNodeIdentity snId, int level) {
        // Send the request through the underlay
        Response response = this.send(destinationAddress, port, new UpdateLeftNodeRequest(level, snId));
        return ((IdentityResponse) response).identity;
    }

    public SkipNodeIdentity getIdentity(String destinationAddress, int port) {
        Response r = send(destinationAddress, port, new GetIdentityRequest());
        return ((IdentityResponse) r).identity;
    }

    public SkipNodeIdentity getLeftNode(String destinationAddress, int port, int level) {
        return getLeftNode(true, destinationAddress, port, level);
    }

    public SkipNodeIdentity getRightNode(String destinationAddress, int port, int level) {
        return getRightNode(true, destinationAddress, port, level);
    }

    public SkipNodeIdentity getLeftNode(boolean backoff, String destinationAddress, int port, int level) {
        // Send the request through the underlay
        GetLeftNodeRequest req = new GetLeftNodeRequest(level);
        req.backoff = backoff;
        Response r = send(destinationAddress, port, req);
        // If the client has returned a locked response (i.e., has indicated that we should try again), return
        // an invalid skip node identity.
        if(r.locked) return LookupTable.INVALID_NODE;
        return ((IdentityResponse) r).identity;
    }

    public SkipNodeIdentity getRightNode(boolean backoff, String destinationAddress, int port, int level) {
        // Send the request through the underlay
        GetRightNodeRequest req = new GetRightNodeRequest(level);
        req.backoff = backoff;
        Response r = send(destinationAddress, port, req);
        // If the client has returned a locked response (i.e., has indicated that we should try again), return
        // an invalid skip node identity.
        if(r.locked) return LookupTable.INVALID_NODE;
        return ((IdentityResponse) r).identity;
    }

    public TentativeTable acquireNeighbors(String destinationAddress, int port, SkipNodeIdentity newNodeID, int level) {
        // Send the request through the underlay
        Response r = send(destinationAddress, port, new AcquireNeighborsRequest(newNodeID, level));
        return ((TableResponse) r).table;
    }

    public SkipNodeIdentity findLadder(String destinationAddress, int port, int level, int direction, String target) {
        // Send the request through the underlay
        Response r = send(destinationAddress, port, new FindLadderRequest(level, direction, target));
        return ((IdentityResponse) r).identity;
    }

    public void announceNeighbor(String destinationAddress, int port, SkipNodeIdentity newNeighbor, int minLevel) {
        // Send the request through the underlay
        send(destinationAddress, port, new AnnounceNeighborRequest(newNeighbor, minLevel));
    }

    public boolean isAvailable(String destinationAddress, int port) {
        Response r = send(destinationAddress, port, new IsAvailableRequest());
        return ((BooleanResponse) r).answer;
    }

    public SkipNodeIdentity getLeftLadder(String destinationAddress, int port, int level, String nameID) {
        // Send the request through the underlay
        Response r = send(destinationAddress, port, new GetLeftLadderRequest(level, nameID));
        return ((IdentityResponse) r).identity;
    }

    public SkipNodeIdentity getRightLadder(String destinationAddress, int port, int level, String nameID) {
        // Send the request through the underlay
        Response r = send(destinationAddress, port, new GetRightLadderRequest(level, nameID));
        return ((IdentityResponse) r).identity;
    }

    public SkipNodeIdentity increment(String destinationAddress, int port, SkipNodeIdentity snId, int level){
        // Send the request through the underlay
        try{
            Thread.sleep(10000);
        }catch (Exception e){
            e.printStackTrace();
        }
        Response response = send(destinationAddress, port, new IncrementRequest(level, snId));
        if (response==null){
            System.exit(1);
        }
        return ((IdentityResponse) response).identity;
    }

    public boolean inject(String destinationAddress, int port, List<SkipNodeIdentity> snIds){
        // Send the request through the underlay
        Response response = send(destinationAddress, port, new InjectionRequest(snIds));
        if (response==null){
            System.exit(1);
        }
        return ((BooleanResponse) response).answer;
    }
}
