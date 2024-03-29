package underlay.packets.requests;

import model.identifier.MembershipVector;
import underlay.packets.Request;
import underlay.packets.RequestType;

/**
 * Request for searching by membership vector.
 */
public class SearchByMembershipVectorRequest extends Request {

  public final MembershipVector targetMembershipVector;

  public SearchByMembershipVectorRequest(MembershipVector targetMembershipVector) {
    super(RequestType.SearchByMembershipVector);
    this.targetMembershipVector = targetMembershipVector;
  }
}
