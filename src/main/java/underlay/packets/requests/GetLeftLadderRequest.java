package underlay.packets.requests;

import model.identifier.MembershipVector;
import underlay.packets.Request;
import underlay.packets.RequestType;

/**
 * Request for getting left ladder.
 */
public class GetLeftLadderRequest extends Request {

  public final int level;
  public final MembershipVector membershipVector;

  /**
   * Constructor for GetLeftLadderRequest.
   *
   * @param level  Integer representing the level.
   * @param membershipVector The membership vector of the node.
   */
  public GetLeftLadderRequest(int level, MembershipVector membershipVector) {
    super(RequestType.GetLeftLadder);
    this.level = level;
    this.membershipVector = membershipVector;
  }
}
