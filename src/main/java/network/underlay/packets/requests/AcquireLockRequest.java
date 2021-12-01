package network.underlay.packets.requests;

import network.underlay.packets.Request;
import network.underlay.packets.RequestType;
import skipnode.SkipNodeIdentity;

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
   * @param version   Integer representing the version.
   */
  public AcquireLockRequest(SkipNodeIdentity requester, int version) {
    super(RequestType.AcquireLock);
    this.requester = requester;
    this.version = version;
  }
}
