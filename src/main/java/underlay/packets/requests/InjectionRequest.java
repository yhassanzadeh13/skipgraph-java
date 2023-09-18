package underlay.packets.requests;

import java.util.List;

import skipnode.Identity;
import underlay.packets.Request;
import underlay.packets.RequestType;

/**
 * Request for injection.
 */
public class InjectionRequest extends Request {

  public final List<Identity> snIds;

  public InjectionRequest(List<Identity> snIds) {
    super(RequestType.Injection);
    this.snIds = snIds;
  }
}
