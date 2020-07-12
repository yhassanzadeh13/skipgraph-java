package underlay.packets;

import java.io.Serializable;

public class Request implements Serializable {

    public final RequestType type;

    public Request(RequestType type) {
        this.type = type;
    }
}
