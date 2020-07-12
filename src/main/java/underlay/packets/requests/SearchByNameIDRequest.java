package underlay.packets.requests;

import underlay.packets.Request;
import underlay.packets.RequestType;

public class SearchByNameIDRequest extends Request {

    public final String targetNameID;

    public SearchByNameIDRequest(String targetNameID) {
        super(RequestType.SearchByNameID);
        this.targetNameID = targetNameID;
    }
}
