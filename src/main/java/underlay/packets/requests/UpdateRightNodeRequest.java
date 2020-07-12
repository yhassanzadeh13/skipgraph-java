package underlay.packets.requests;

import skipnode.SkipNodeIdentity;
import underlay.packets.Request;
import underlay.packets.RequestType;

public class UpdateRightNodeRequest extends Request {

    public final int level;
    public final SkipNodeIdentity snId;

    public UpdateRightNodeRequest(int level, SkipNodeIdentity snId) {
        super(RequestType.UpdateRightNode);
        this.level = level;
        this.snId = snId;
    }
}
