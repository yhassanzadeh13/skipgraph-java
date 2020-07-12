package underlay.packets.requests;

import underlay.packets.Request;
import underlay.packets.RequestType;

public class SearchByNumIDRequest extends Request {

    public final int targetNumID;

    public SearchByNumIDRequest(int targetNumID) {
        super(RequestType.SearchByNumID);
        this.targetNumID = targetNumID;
    }
}
