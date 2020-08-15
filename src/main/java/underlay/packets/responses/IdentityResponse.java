package underlay.packets.responses;

import skipnode.SkipNodeIdentity;
import underlay.packets.Response;

public class IdentityResponse extends Response {

    public final SkipNodeIdentity identity;

    public IdentityResponse(SkipNodeIdentity identity) {
        this.identity = identity;
    }
}
