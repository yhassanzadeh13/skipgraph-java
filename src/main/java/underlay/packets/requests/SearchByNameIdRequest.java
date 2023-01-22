package underlay.packets.requests;

import model.identifier.MembershipVector;
import underlay.packets.Request;
import underlay.packets.RequestType;

/**
 * Request for searching by name id.
 */
public class SearchByNameIdRequest extends Request {

  public final MembershipVector targetNameId;

  public SearchByNameIdRequest(MembershipVector targetNameId) {
    super(RequestType.SearchByNameId);
    this.targetNameId = targetNameId;
  }
}
