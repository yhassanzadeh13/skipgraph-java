package network.packets.requests;

import java.util.List;

import skipnode.SkipNodeIdentity;
import network.packets.Request;
import network.packets.RequestType;

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
