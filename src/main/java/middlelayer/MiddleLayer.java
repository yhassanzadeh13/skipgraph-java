package middlelayer;
import skipnode.SkipNodeIdentity;
import skipnode.SkipNodeInterface;
import underlay.Underlay;
import underlay.packets.*;

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
     * @param t type of the request.
     * @param parameters request parameters.
     * @return the response emitted by the remote client.
     */
    public ResponseParameters send(String destinationAddress, int port, RequestType t, RequestParameters parameters) {
        // TODO: check if the dest. address is identical to the address of this node
        //  and relay the request to a different local node.
        return underlay.sendMessage(destinationAddress, port, t, parameters);
    }

    /**
     * Called by the underlay to collect the response from the overlay.
     * @return response emitted by the overlay.
     */
    public ResponseParameters receive(RequestType t, RequestParameters p) {
        // TODO: Create responses for each request type
        GenericResponse resp = new GenericResponse();
        switch (t) {
            case SearchByNameID:
                SkipNodeIdentity result = overlay.searchByNameID((String) p.getRequestValue("targetNameID"));
                resp.addParameter("searchResult", result);
                return resp;
            case SearchByNumID:
                resp.addParameter("searchResult", overlay.searchByNumID((Integer) p.getRequestValue("targetNumID")));
                return resp;
            case NameIDLevelSearch:
                resp.addParameter("searchResult", overlay.nameIDLevelSearch((Integer) p.getRequestValue("level"),
                                                                            (String) p.getRequestValue("targetNameID")));
                return resp;
            case UpdateLeftNode:
                resp.addParameter("successful", overlay.updateLeftNode((SkipNodeIdentity) p.getRequestValue("newValue"),
                                                                         (Integer) p.getRequestValue("level")));
                return resp;
            case UpdateRightNode:
                resp.addParameter("successful", overlay.updateRightNode((SkipNodeIdentity) p.getRequestValue("newValue"),
                                                                         (Integer) p.getRequestValue("level")));
                return resp;
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

    public SkipNodeIdentity searchByNameID(String destinationAddress, int port, String nameID){
        // Create the request
        GenericRequest req = new GenericRequest();
        // Add the parameters
        req.addParameter("targetNameID", nameID);

        // Send the request through the underlay
        ResponseParameters response = this.send(destinationAddress, port, RequestType.SearchByNameID, req);

        return (SkipNodeIdentity) response.getResponseValue("searchResult");
    }

    public SkipNodeIdentity searchByNumID(String destinationAddress, int port, int numID){
        // Create the request
        GenericRequest req = new GenericRequest();
        // Add the parameters
        req.addParameter("targetNumID", numID);

        // Send the request through the underlay
        ResponseParameters response = this.send(destinationAddress, port, RequestType.SearchByNumID, req);

        return (SkipNodeIdentity) response.getResponseValue("searchResult");
    }

    public SkipNodeIdentity nameIDLevelSearch(String destinationAddress, int port, int level, String nameID){
        // Create the request
        GenericRequest req = new GenericRequest();
        // Add the parameters
        req.addParameter("level", level);
        req.addParameter("targetNameID", nameID);

        // Send the request through the underlay
        ResponseParameters response = this.send(destinationAddress, port, RequestType.NameIDLevelSearch, req);

        return (SkipNodeIdentity) response.getResponseValue("searchResult");
    }

    public SkipNodeIdentity updateRightNode(String destinationAddress, int port,SkipNodeIdentity snId, int level){
        // Create the request
        UpdateRequest req = new UpdateRequest(level, snId);

        // Send the request through the underlay
        ResponseParameters response = this.send(destinationAddress, port, RequestType.UpdateRightNode, req);

        return (SkipNodeIdentity) response.getResponseValue("searchResult");
    }

    public SkipNodeIdentity updateLeftNode(String destinationAddress, int port,SkipNodeIdentity snId, int level){
        // Create the request
        UpdateRequest req = new UpdateRequest(level, snId);

        // Send the request through the underlay
        ResponseParameters response = this.send(destinationAddress, port, RequestType.UpdateLeftNode, req);

        return (SkipNodeIdentity) response.getResponseValue("searchResult");
    }
}
