package underlay.packets.requests;

import underlay.packets.Request;
import underlay.packets.RequestType;

public class GetLeftNodeRequest extends Request {

    public final int level;

    public GetLeftNodeRequest(int level) {
        super(RequestType.GetLeftNode);
        this.level = level;
    }
}
