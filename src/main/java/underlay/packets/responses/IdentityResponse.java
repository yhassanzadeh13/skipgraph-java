package underlay.packets.responses;

import model.identifier.Identity;
import underlay.packets.Response;

/**
 * Response for identity request.
 */
public class IdentityResponse extends Response {

  public final Identity identity;

  public IdentityResponse(Identity identity) {
    this.identity = identity;
  }
}
