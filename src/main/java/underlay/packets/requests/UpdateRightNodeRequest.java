package underlay.packets.requests;

import skipnode.SkipNodeIdentity;
import underlay.packets.Request;
import underlay.packets.RequestType;

/**
 * Request for updating right node.
 */
public class UpdateRightNodeRequest extends Request {

  public final int level;
  public final SkipNodeIdentity snId;

  /**
   * Constructor for UpdateRightNodeRequest.
   *
   * @param level Integer representing the level.
   * @param snId Skipnode identity.
   */
  public UpdateRightNodeRequest(int level, SkipNodeIdentity snId) {
    super(RequestType.UpdateRightNode);
    this.level = level;
    this.snId = snId;
  }
}
