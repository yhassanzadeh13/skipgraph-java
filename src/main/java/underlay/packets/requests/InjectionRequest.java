package underlay.packets.requests;

import skipnode.SkipNodeIdentity;
import underlay.packets.Request;
import underlay.packets.RequestType;

import java.util.List;

public class InjectionRequest extends Request {

    public final List<SkipNodeIdentity> snIds;
    public InjectionRequest(List<SkipNodeIdentity> snIds) {
        super(RequestType.Injection);
        this.snIds=snIds;
    }
}
