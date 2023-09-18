package underlay.packets.requests;

import model.identifier.Identity;
import underlay.packets.Request;
import underlay.packets.RequestType;

/**
 * Request for acquiring a lock.
 */
public class AcquireLockRequest extends Request {

  public final Identity requester;

  /**
   * Constructor for AcquireLockRequest.
   *
   * @param requester node that is requesting the lock.
   */
  public AcquireLockRequest(Identity requester) {
    super(RequestType.AcquireLock);
    this.requester = requester;
  }
}
