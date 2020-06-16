package underlay.tcp;

import underlay.packets.ResponseParameters;

import java.io.Serializable;

/**
 * Wraps the response in a class that can be sent across the wire.
 */
public class TCPResponse implements Serializable {

    public final ResponseParameters parameters;

    public TCPResponse(ResponseParameters parameters) {
        this.parameters = parameters;
    }
}
