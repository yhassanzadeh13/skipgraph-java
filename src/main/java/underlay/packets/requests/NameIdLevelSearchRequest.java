package underlay.packets.requests;

import model.identifier.MembershipVector;
import underlay.packets.Request;
import underlay.packets.RequestType;

/**
 * Request for name id level search.
 */
public class NameIdLevelSearchRequest extends Request {

  public final int level;
  public final int direction;
  public final MembershipVector target;

  /**
   * Constructor for NameIdLevelSearchRequest.
   *
   * @param level     Integer representing the level.
   * @param direction Integer representing the direction.
   * @param target    membership vector of the target node.
   */
  public NameIdLevelSearchRequest(int level, int direction, MembershipVector target) {
    super(RequestType.MembershipVectorLevelSearch);
    this.level = level;
    this.direction = direction;
    this.target = target;
  }
}
