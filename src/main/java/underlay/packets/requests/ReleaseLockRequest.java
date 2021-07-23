package underlay.packets.requests;

import skipnode.SkipNodeIdentity;
import underlay.packets.Request;
import underlay.packets.RequestType;

/**
 * Request for releasing lock.
 */
public class ReleaseLockRequest extends Request {

  public final SkipNodeIdentity owner;

  public ReleaseLockRequest(SkipNodeIdentity owner) {
    super(RequestType.ReleaseLock);
    this.owner = owner;
  }
}
