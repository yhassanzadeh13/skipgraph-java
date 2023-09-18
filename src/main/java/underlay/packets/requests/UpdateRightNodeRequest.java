package underlay.packets.requests;

import model.identifier.Identity;
import underlay.packets.Request;
import underlay.packets.RequestType;

/**
 * Request for updating right node.
 */
public class UpdateRightNodeRequest extends Request {

  public final int level;
  public final Identity snId;

  /**
   * Constructor for UpdateRightNodeRequest.
   *
   * @param level Integer representing the level.
   * @param snId  node identity.
   */
  public UpdateRightNodeRequest(int level, Identity snId) {
    super(RequestType.UpdateRightNode);
    this.level = level;
    this.snId = snId;
  }
}
