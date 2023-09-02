package underlay.packets.requests;

import model.identifier.MembershipVector;
import underlay.packets.Request;
import underlay.packets.RequestType;

/**
 * Request for getting right ladder.
 */
public class GetRightLadderRequest extends Request {
  public final int level;
  public final MembershipVector membershipVector;

  /**
   * Constructor for GetRightLadderRequest.
   *
   * @param level  Integer representing level.
   * @param membershipVector the membership vector of the node.
   */
  public GetRightLadderRequest(int level, MembershipVector membershipVector) {
    super(RequestType.GetRightLadder);
    this.level = level;
    this.membershipVector = membershipVector;
  }
}
