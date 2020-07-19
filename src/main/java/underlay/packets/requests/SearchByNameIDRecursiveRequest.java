package underlay.packets.requests;

import skipnode.SkipNodeIdentity;
import underlay.packets.Request;
import underlay.packets.RequestType;

public class SearchByNameIDRecursiveRequest extends Request {

    public final SkipNodeIdentity left;
    public final SkipNodeIdentity right;
    public final String target;
    public final int level;

    public SearchByNameIDRecursiveRequest(SkipNodeIdentity left, SkipNodeIdentity right, String target, int level) {
        super(RequestType.SearchByNameIDRecursive);
        this.left = left;
        this.right = right;
        this.target = target;
        this.level = level;
    }
}
