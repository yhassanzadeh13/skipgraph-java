package underlay.packets.requests;

import underlay.packets.Request;
import underlay.packets.RequestType;

/**
 * Request for getting right ladder.
 */
public class GetRightLadderRequest extends Request {

  public final int level;
  public final String nameId;

  /**
   * Constructor for GetRightLadderRequest.
   *
   * @param level Integer representing level.
   * @param nameId Name id of the node.
   */
  public GetRightLadderRequest(int level, String nameId) {
    super(RequestType.GetRightLadder);
    this.level = level;
    this.nameId = nameId;
  }
}
