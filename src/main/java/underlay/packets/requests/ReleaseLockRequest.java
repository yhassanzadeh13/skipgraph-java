package underlay.packets.requests;

import model.identifier.Identity;
import underlay.packets.Request;
import underlay.packets.RequestType;

/**
 * Request for releasing lock.
 */
public class ReleaseLockRequest extends Request {

  public final Identity owner;

  public ReleaseLockRequest(Identity owner) {
    super(RequestType.ReleaseLock);
    this.owner = owner;
  }
}
