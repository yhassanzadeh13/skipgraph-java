package underlay.packets.requests;

import skipnode.SkipNodeIdentity;
import underlay.packets.Request;
import underlay.packets.RequestType;

public class UpdateLeftNodeRequest extends Request {

    public final int level;
    public final SkipNodeIdentity snId;

    public UpdateLeftNodeRequest(int level, SkipNodeIdentity snId) {
        super(RequestType.UpdateLeftNode);
        this.level = level;
        this.snId = snId;
    }

}
