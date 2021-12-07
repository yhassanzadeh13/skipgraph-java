package network.underlay.packets.responses;

import network.underlay.packets.Response;
import skipnode.SkipNodeIdentity;

/**
 * Response for identity request.
 */
public class IdentityResponse extends Response {

  public final SkipNodeIdentity identity;

  public IdentityResponse(SkipNodeIdentity identity) {
    this.identity = identity;
  }
}
