package underlay.packets;

import java.io.Serializable;

/**
 * Represents a serializable request packet. Every request type must inherit from this class.
 */
public class Request implements Serializable {

    public final RequestType type;

    public Request(RequestType type) {
        this.type = type;
    }
}
