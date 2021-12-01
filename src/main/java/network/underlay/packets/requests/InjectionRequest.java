package network.underlay.packets.requests;

import java.util.List;

import network.underlay.packets.Request;
import network.underlay.packets.RequestType;
import skipnode.SkipNodeIdentity;

/**
 * Request for injection.
 */
public class InjectionRequest extends Request {

  public final List<SkipNodeIdentity> snIds;

  public InjectionRequest(List<SkipNodeIdentity> snIds) {
    super(RequestType.Injection);
    this.snIds = snIds;
  }
}
