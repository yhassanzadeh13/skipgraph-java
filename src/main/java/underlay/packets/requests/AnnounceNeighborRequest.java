package underlay.packets.requests;

import skipnode.SkipNodeIdentity;
import underlay.packets.Request;
import underlay.packets.RequestType;

public class AnnounceNeighborRequest extends Request {

    public final SkipNodeIdentity newNeighbor;
    public final int minLevel;

    public AnnounceNeighborRequest(SkipNodeIdentity newNeighbor, int minLevel) {
        super(RequestType.AnnounceNeighbor);
        this.newNeighbor = newNeighbor;
        this.minLevel = minLevel;
    }
}
