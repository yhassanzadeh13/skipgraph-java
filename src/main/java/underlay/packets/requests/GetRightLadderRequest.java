package underlay.packets.requests;

import underlay.packets.Request;
import underlay.packets.RequestType;

public class GetRightLadderRequest extends Request {

    public final int level;
    public final String nameID;

    public GetRightLadderRequest(int level, String nameID) {
        super(RequestType.GetRightLadder);
        this.level = level;
        this.nameID = nameID;
    }
}
