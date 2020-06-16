package underlay;

import underlay.packets.AckResponse;
import underlay.packets.RequestParameters;
import underlay.packets.RequestType;
import underlay.packets.ResponseParameters;

/**
 * Represents a handler that can emit responses according to the allowed request types. Methods can be called
 * asynchronously, thus this class should not contain any state or they must be implemented in a thread-safe manner.
 * New request types should be handled here by (1) implementing a new method, and (2) adding the appropriate case in the
 * `dispatchRequest` method.
 */
public class RequestHandler {

    /**
     * Dispatches a request to the appropriate handler method.
     * @param t type of the request.
     * @param p request parameters.
     * @return emitted response.
     */
    public ResponseParameters dispatchRequest(RequestType t, RequestParameters p) {
        switch (t) {
            case SearchByNameID:
                return searchByNameID((String) p.getRequestValue("targetNameID"));
            case SearchByNumID:
                return searchByNumID((Integer) p.getRequestValue("targetNumID"));
            case NameIDLevelSearch:
                return nameIDLevelSearch((Integer) p.getRequestValue("level"),
                        (String) p.getRequestValue("targetNameID"));
            case UpdateLeftNode:
                return updateLeftNode((Integer) p.getRequestValue("level"),
                        (String) p.getRequestValue("newValue"));
            case UpdateRightNode:
                return updateRightNode((Integer) p.getRequestValue("level"),
                        (String) p.getRequestValue("newValue"));
        }
        return null;
    }

    public ResponseParameters searchByNameID(String targetNameID) {
        // TODO: send to overlay
        return new AckResponse();
    }

    public ResponseParameters searchByNumID(int targetNumID) {
        // TODO: send to overlay
        return new AckResponse();
    }

    public ResponseParameters nameIDLevelSearch(int level, String targetNameID) {
        // TODO: send to overlay
        return new AckResponse();
    }

    public ResponseParameters updateLeftNode(int level, String newValue) {
        // TODO: send to overlay
        return new AckResponse();
    }

    public ResponseParameters updateRightNode(int level, String newValue) {
        // TODO: send to overlay
        return new AckResponse();
    }

}
