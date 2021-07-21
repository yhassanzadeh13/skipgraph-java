package underlay.packets.requests;

import underlay.packets.Request;
import underlay.packets.RequestType;

public class GetIdentityRequest extends Request {

  public GetIdentityRequest() {
    super(RequestType.GetIdentity);
  }
}
