package underlay.packets.requests;

import skipnode.SkipNodeIdentity;
import underlay.packets.Request;
import underlay.packets.RequestType;

public class AnnounceNeighborRequest extends Request {

    public final SkipNodeIdentity newNeighbor;

    public AnnounceNeighborRequest(SkipNodeIdentity newNeighbor) {
        super(RequestType.AnnounceNeighbor);
        this.newNeighbor = newNeighbor;
    }
}
