package underlay.packets.responses;

import skipnode.SkipNodeIdentity;
import underlay.packets.Response;

public class SkipNodeIdentityResponse extends Response {

    public final SkipNodeIdentity identity;

    public SkipNodeIdentityResponse(SkipNodeIdentity identity) {
        this.identity = identity;
    }
}
