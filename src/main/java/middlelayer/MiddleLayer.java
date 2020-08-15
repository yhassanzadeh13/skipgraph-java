package middlelayer;
import skipnode.SkipNodeIdentity;
import skipnode.SkipNodeInterface;
import underlay.Underlay;
import underlay.packets.*;
import underlay.packets.requests.*;
import underlay.packets.responses.IdentityListResponse;
import underlay.packets.responses.IdentityResponse;

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
        // Bounce the request up.
        if(destinationAddress.equals(underlay.getAddress()) && port == underlay.getPort()) {
            return receive(request);
        }
        return underlay.sendMessage(destinationAddress, port, request);
    }

    /**
     * Called by the underlay to collect the response from the overlay.
     * @return response emitted by the overlay.
     */
    public Response receive(Request request) {
        SkipNodeIdentity identity;
        switch (request.type) {
            case SearchByNameID:
                identity = overlay.searchByNameID(((SearchByNameIDRequest) request).targetNameID);
                return new IdentityResponse(identity);
            case SearchByNameIDRecursive:
                identity = overlay.searchByNameIDRecursive(((SearchByNameIDRecursiveRequest) request).left,
                        ((SearchByNameIDRecursiveRequest) request).right,
                        ((SearchByNameIDRecursiveRequest) request).target,
                        ((SearchByNameIDRecursiveRequest) request).level);
                return new IdentityResponse(identity);
            case SearchByNumID:
                identity = overlay.searchByNumID(((SearchByNumIDRequest) request).targetNumID);
                return new IdentityResponse(identity);
            case NameIDLevelSearch:
                identity = overlay.nameIDLevelSearch(((NameIDLevelSearchRequest) request).level,
                        ((NameIDLevelSearchRequest) request).direction,
                        ((NameIDLevelSearchRequest) request).targetNameID);
                return new IdentityResponse(identity);
            case UpdateLeftNode:
                identity = overlay.updateLeftNode(((UpdateLeftNodeRequest) request).snId, ((UpdateLeftNodeRequest) request).level);
                return new IdentityResponse(identity);
            case UpdateRightNode:
                identity = overlay.updateRightNode(((UpdateRightNodeRequest) request).snId, ((UpdateRightNodeRequest) request).level);
                return new IdentityResponse(identity);
            case GetRightNode:
                identity = overlay.getRightNode(((GetRightNodeRequest) request).level);
                return new IdentityResponse(identity);
            case GetLeftNode:
                identity = overlay.getLeftNode(((GetLeftNodeRequest) request).level);
                return new IdentityResponse(identity);
            case GetPotentialNeighbors:
                List<SkipNodeIdentity> neighbors = overlay.getPotentialNeighbors(((GetPotentialNeighborsRequest) request).newNode,
                        ((GetPotentialNeighborsRequest) request).level);
                return new IdentityListResponse(neighbors);
            case FindLadder:
                identity = overlay.findLadder(((FindLadderRequest) request).level, ((FindLadderRequest) request).direction,
                        ((FindLadderRequest) request).target);
                return new IdentityResponse(identity);
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

    public SkipNodeIdentity searchByNameID(String destinationAddress, int port, String nameID) {
        // Send the request through the underlay
        Response response = this.send(destinationAddress, port, new SearchByNameIDRequest(nameID));
        return ((IdentityResponse) response).identity;
    }

    public SkipNodeIdentity searchByNameIDRecursive(String destinationAddress, int port, SkipNodeIdentity left,
                                                    SkipNodeIdentity right, String target, int level) {
        // Send the request through the underlay.
        Response response = this.send(destinationAddress, port, new SearchByNameIDRecursiveRequest(left, right, target, level));
        return ((IdentityResponse) response).identity;
    }

    public SkipNodeIdentity searchByNumID(String destinationAddress, int port, int numID) {
        // Send the request through the underlay
        Response response = this.send(destinationAddress, port, new SearchByNumIDRequest(numID));
        return ((IdentityResponse) response).identity;
    }

    public SkipNodeIdentity nameIDLevelSearch(String destinationAddress, int port, int level, int direction, String nameID) {
        // Send the request through the underlay
        Response response = this.send(destinationAddress, port, new NameIDLevelSearchRequest(level, direction, nameID));
        return ((IdentityResponse) response).identity;
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

    public SkipNodeIdentity getLeftNode(String destinationAddress, int port, int level) {
        Response r = send(destinationAddress, port, new GetLeftNodeRequest(level));
        return ((IdentityResponse) r).identity;
    }

    public SkipNodeIdentity getRightNode(String destinationAddress, int port, int level) {
        Response r = send(destinationAddress, port, new GetRightNodeRequest(level));
        return ((IdentityResponse) r).identity;
    }

    public List<SkipNodeIdentity> getPotentialNeighbors(String destinationAddress, int port, SkipNodeIdentity newNodeID, int level) {
        Response r = send(destinationAddress, port, new GetPotentialNeighborsRequest(newNodeID, level));
        return ((IdentityListResponse) r).identities;
    }

    public SkipNodeIdentity findLadder(String destinationAddress, int port, int level, int direction, String target) {
        Response r = send(destinationAddress, port, new FindLadderRequest(level, direction, target));
        return ((IdentityResponse) r).identity;
    }
}
