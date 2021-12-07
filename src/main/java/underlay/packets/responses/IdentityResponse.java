package underlay.packets.responses;

import skipnode.SkipNodeIdentity;
import underlay.packets.Response;

/** Response for identity request. */
public class IdentityResponse extends Response {

  public final SkipNodeIdentity identity;

  public IdentityResponse(SkipNodeIdentity identity) {
    this.identity = identity;
  }
}
