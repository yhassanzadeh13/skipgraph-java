package underlay.packets.requests;

import underlay.packets.Request;
import underlay.packets.RequestType;

public class GetLeftLadderRequest extends Request {

    public final int level;
    public final String nameID;

    public GetLeftLadderRequest(int level, String nameID) {
        super(RequestType.GetLeftLadder);
        this.level = level;
        this.nameID = nameID;
    }
}
