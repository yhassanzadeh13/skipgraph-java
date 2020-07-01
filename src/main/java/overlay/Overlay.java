package overlay;

import middlelayer.MiddleLayer;
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
public class Overlay {

    // Use to send requests.
    private MiddleLayer middleLayer;

    public void setMiddleLayer(MiddleLayer middleLayer) {
        this.middleLayer = middleLayer;
    }

    public ResponseParameters searchByNameID(String targetNameID) {
        // TODO: handle
        return new AckResponse();
    }

    public ResponseParameters searchByNumID(int targetNumID) {
        // TODO: handle
        return new AckResponse();
    }

    public ResponseParameters nameIDLevelSearch(int level, String targetNameID) {
        // TODO: handle
        return new AckResponse();
    }

    public ResponseParameters updateLeftNode(int level, String newValue) {
        // TODO: handle
        return new AckResponse();
    }

    public ResponseParameters updateRightNode(int level, String newValue) {
        // TODO: handle
        return new AckResponse();
    }

}
