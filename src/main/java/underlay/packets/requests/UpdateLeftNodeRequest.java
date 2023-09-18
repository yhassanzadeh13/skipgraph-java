package underlay.packets.requests;

import skipnode.Identity;
import underlay.packets.Request;
import underlay.packets.RequestType;

/**
 * Request for updating left node.
 */
public class UpdateLeftNodeRequest extends Request {

  public final int level;
  public final Identity snId;

  /**
   * Constructor for UpdateLeftNodeRequest.
   *
   * @param level Integer representing the level.
   * @param snId  Skipnode identity.
   */
  public UpdateLeftNodeRequest(int level, Identity snId) {
    super(RequestType.UpdateLeftNode);
    this.level = level;
    this.snId = snId;
  }
}
