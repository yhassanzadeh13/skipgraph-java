package underlay.packets.requests;

import skipnode.SkipNodeIdentity;
import underlay.packets.Request;
import underlay.packets.RequestType;

public class GetPotentialNeighborsRequest extends Request {

    public final SkipNodeIdentity newNode;
    public final int level;

    public GetPotentialNeighborsRequest(SkipNodeIdentity newNode, int level) {
        super(RequestType.GetPotentialNeighbors);
        this.newNode = newNode;
        this.level = level;
    }
}
