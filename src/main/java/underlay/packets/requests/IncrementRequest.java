package underlay.packets.requests;

import skipnode.Identity;
import underlay.packets.Request;
import underlay.packets.RequestType;

/**
 * Request for increment.
 */
public class IncrementRequest extends Request {

  public final int level;
  public final Identity snId;

  /**
   * Constructor for IncrementRequest.
   *
   * @param level Integer representing level.
   * @param snId  Skipnode identity
   */
  public IncrementRequest(int level, Identity snId) {
    super(RequestType.Increment);
    this.level = level;
    this.snId = snId;
  }
}
