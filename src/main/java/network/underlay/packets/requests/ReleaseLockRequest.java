package network.underlay.packets.requests;

import network.underlay.packets.Request;
import network.underlay.packets.RequestType;
import skipnode.SkipNodeIdentity;

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
