package underlay.packets.requests;

import underlay.packets.Request;
import underlay.packets.RequestType;

/**
 * Request for name id level search.
 */
public class NameIdLevelSearchRequest extends Request {

  public final int level;
  public final int direction;
  public final String targetNameId;

  /**
   * Constructor for NameIdLevelSearchRequest.
   *
   * @param level Integer representing the level.
   * @param direction Integer representing the direction.
   * @param targetNameId name id of the target node.
   */
  public NameIdLevelSearchRequest(int level, int direction, String targetNameId) {
    super(RequestType.NameIdLevelSearch);
    this.level = level;
    this.direction = direction;
    this.targetNameId = targetNameId;
  }
}
