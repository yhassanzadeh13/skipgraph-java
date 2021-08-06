package underlay.packets.requests;

import skipnode.SkipNodeIdentity;
import underlay.packets.Request;
import underlay.packets.RequestType;

/**
 * Request for acquiring a lock.
 */
public class AcquireLockRequest extends Request {

  public final SkipNodeIdentity requester;
  // The requester needs to have the correct version of the server in order to acquire the lock.
  public final int version;

  /**
   * Constructor for AcquireLockRequest.
   *
   * @param requester Skipnode that is requesting the lock.
   * @param version Integer representing the version.
   */
  public AcquireLockRequest(SkipNodeIdentity requester, int version) {
    super(RequestType.AcquireLock);
    this.requester = requester;
    this.version = version;
  }
}
