package underlay.packets.requests;

import underlay.packets.Request;
import underlay.packets.RequestType;

/**
 * Request for getting identity.
 */
public class GetIdentityRequest extends Request {

  public GetIdentityRequest() {
    super(RequestType.GetIdentity);
  }
}
