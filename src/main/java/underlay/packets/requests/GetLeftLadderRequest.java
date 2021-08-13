package underlay.packets.requests;

import underlay.packets.Request;
import underlay.packets.RequestType;

/** Request for getting left ladder. */
public class GetLeftLadderRequest extends Request {

  public final int level;
  public final String nameId;

  /**
   * Constructor for GetLeftLadderRequest.
   *
   * @param level Integer representing the level.
   * @param nameId Name ID of the node.
   */
  public GetLeftLadderRequest(int level, String nameId) {
    super(RequestType.GetLeftLadder);
    this.level = level;
    this.nameId = nameId;
  }
}
