package underlay.packets.requests;

import skipnode.SkipNodeIdentity;
import underlay.packets.Request;
import underlay.packets.RequestType;

import java.util.List;

public class SearchByNameIDRecursiveRequest extends Request {

    public final String target;
    public final int level;

    public SearchByNameIDRecursiveRequest(String target, int level) {
        super(RequestType.SearchByNameIDRecursive);
        this.target = target;
        this.level = level;
    }
}
