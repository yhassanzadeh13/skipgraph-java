package underlay.tcp;

import underlay.packets.RequestParameters;
import underlay.packets.RequestType;

import java.io.Serializable;

/**
 * Wraps the request in a single class that can be sent across the wire.
 */
public class TCPRequest implements Serializable {

    // Type of the request.
    public final RequestType type;
    // Request parameters.
    public final RequestParameters parameters;

    public TCPRequest(RequestType type, RequestParameters parameters) {
        this.type = type;
        this.parameters = parameters;
    }
}
