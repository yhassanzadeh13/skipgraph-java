package underlay.packets.responses;

import skipnode.SkipNodeIdentity;
import underlay.packets.Response;

import java.util.List;

public class IdentityListResponse extends Response {

    public final List<SkipNodeIdentity> identities;

    public IdentityListResponse(List<SkipNodeIdentity> identities) {
        this.identities = identities;
    }
}
