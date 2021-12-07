package network.underlay.packets.requests;

import network.underlay.packets.Request;
import network.underlay.packets.RequestType;

/** Request for getting identity. */
public class GetIdentityRequest extends Request {

  public GetIdentityRequest() {
    super(RequestType.GetIdentity);
  }
}
