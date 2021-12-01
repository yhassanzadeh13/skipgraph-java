package network.underlay.packets.requests;

import network.underlay.packets.Request;
import network.underlay.packets.RequestType;
import skipnode.SkipNodeIdentity;

/**
 * Request for increment.
 */
public class IncrementRequest extends Request {

  public final int level;
  public final SkipNodeIdentity snId;

  /**
   * Constructor for IncrementRequest.
   *
   * @param level Integer representing level.
   * @param snId  Skipnode identity
   */
  public IncrementRequest(int level, SkipNodeIdentity snId) {
    super(RequestType.Increment);
    this.level = level;
    this.snId = snId;
  }
}
