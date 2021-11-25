package network.packets.requests;

import network.packets.Request;
import network.packets.RequestType;

/** Request for getting identity. */
public class GetIdentityRequest extends Request {

  public GetIdentityRequest() {
    super(RequestType.GetIdentity);
  }
}
