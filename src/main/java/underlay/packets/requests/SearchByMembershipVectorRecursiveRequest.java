package underlay.packets.requests;

import model.identifier.MembershipVector;
import underlay.packets.Request;
import underlay.packets.RequestType;

/**
 * Request for searching by membership vector recursively.
 */
public class SearchByMembershipVectorRecursiveRequest extends Request {

  public final MembershipVector target;
  public final int level;

  /**
   * Constructor for SearchByMembershipVectorRecursiveRequest.
   *
   * @param target membership vector of the target node.
   * @param level  Integer representing the level.
   */
  public SearchByMembershipVectorRecursiveRequest(MembershipVector target, int level) {
    super(RequestType.SearchByMembershipVectorRecursive);
    this.target = target;
    this.level = level;
  }
}
