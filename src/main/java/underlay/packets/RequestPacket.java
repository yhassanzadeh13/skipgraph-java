package underlay.packets;

import java.io.Serializable;

/**
 * Wraps the request and its type in a single serializable class that can be sent across the wire.
 */
public class RequestPacket implements Serializable {
    // Type of the request.
    public final RequestType type;
    // Request parameters.
    public final RequestParameters parameters;

    public RequestPacket(RequestType type, RequestParameters parameters) {
        this.type = type;
        this.parameters = parameters;
    }
}
