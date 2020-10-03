package underlay.packets.requests;

import skipnode.SkipNodeIdentity;
import underlay.packets.Request;
import underlay.packets.RequestType;

public class IncrementRequest extends Request {

    public final int level;
    public final SkipNodeIdentity snId;

    public IncrementRequest(int level, SkipNodeIdentity snId) {
        super(RequestType.Increment);
        this.level = level;
        this.snId = snId;
    }

}
