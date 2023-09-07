package underlay.packets.requests;

import model.identifier.MembershipVector;
import underlay.packets.Request;
import underlay.packets.RequestType;

/**
 * Request for finding ladder.
 */
public class FindLadderRequest extends Request {

  public final int level;
  public final int direction;
  public final MembershipVector target;

  /**
   * Constructor.
   *
   * @param level     ?.
   * @param direction ?.
   * @param target    ?.
   */
  public FindLadderRequest(int level, int direction, MembershipVector target) {
    super(RequestType.FindLadder);
    this.level = level;
    this.direction = direction;
    this.target = target;
  }
}
