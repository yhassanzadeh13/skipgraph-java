package underlay.packets.requests;

import model.identifier.MembershipVector;
import underlay.packets.Request;
import underlay.packets.RequestType;

/**
 * Request for searching by name id.
 */
public class SearchByMembershipVectorRequest extends Request {

  public final MembershipVector targetMembershipVector;

  public SearchByMembershipVectorRequest(MembershipVector targetNameId) {
    super(RequestType.SearchByMembershipVector);
    this.targetMembershipVector = targetNameId;
  }
}
