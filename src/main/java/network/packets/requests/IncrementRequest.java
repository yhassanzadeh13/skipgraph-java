package network.packets.requests;

import skipnode.SkipNodeIdentity;
import network.packets.Request;
import network.packets.RequestType;

/** Request for increment. */
public class IncrementRequest extends Request {

  public final int level;
  public final SkipNodeIdentity snId;

  /**
   * Constructor for IncrementRequest.
   *
   * @param level Integer representing level.
   * @param snId Skipnode identity
   */
  public IncrementRequest(int level, SkipNodeIdentity snId) {
    super(RequestType.Increment);
    this.level = level;
    this.snId = snId;
  }
}
