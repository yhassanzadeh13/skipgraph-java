package underlay.packets.requests;

import model.identifier.MembershipVector;
import underlay.packets.Request;
import underlay.packets.RequestType;

/** Request for searching by name id recursively. */
public class SearchByNameIdRecursiveRequest extends Request {

  public final MembershipVector target;
  public final int level;

  /**
   * Constructor for SearchByNameIdRecursiveRequest.
   *
   * @param target name id of the target node.
   * @param level Integer representing the level.
   */
  public SearchByNameIdRecursiveRequest(MembershipVector target, int level) {
    super(RequestType.SearchByNameIDRecursive);
    this.target = target;
    this.level = level;
  }
}
