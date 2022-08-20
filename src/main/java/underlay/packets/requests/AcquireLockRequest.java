package underlay.packets.requests;

import skipnode.SkipNodeIdentity;
import underlay.packets.Request;
import underlay.packets.RequestType;

/** Request for acquiring a lock. */
public class AcquireLockRequest extends Request {

  public final SkipNodeIdentity requester;

  /**
   * Constructor for AcquireLockRequest.
   *
   * @param requester Skipnode that is requesting the lock.
   */
  public AcquireLockRequest(SkipNodeIdentity requester) {
    super(RequestType.AcquireLock);
    this.requester = requester;
  }
}
