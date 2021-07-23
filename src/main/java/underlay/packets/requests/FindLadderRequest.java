package underlay.packets.requests;

import underlay.packets.Request;
import underlay.packets.RequestType;

/**
 * Request for finding ladder.
 */
public class FindLadderRequest extends Request {

  public final int level;
  public final int direction;
  public final String target;

  /**
   * Constructor for FindLadderRequest.
   *
   * @param level Integer representing level.
   * @param direction Integer representing direction.
   * @param target String representing the target
   */
  public FindLadderRequest(int level, int direction, String target) {
    super(RequestType.FindLadder);
    this.level = level;
    this.direction = direction;
    this.target = target;
  }
}
