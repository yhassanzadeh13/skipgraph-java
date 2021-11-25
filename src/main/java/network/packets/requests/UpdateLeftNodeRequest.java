package network.packets.requests;

import skipnode.SkipNodeIdentity;
import network.packets.Request;
import network.packets.RequestType;

/** Request for updating left node. */
public class UpdateLeftNodeRequest extends Request {

  public final int level;
  public final SkipNodeIdentity snId;

  /**
   * Constructor for UpdateLeftNodeRequest.
   *
   * @param level Integer representing the level.
   * @param snId Skipnode identity.
   */
  public UpdateLeftNodeRequest(int level, SkipNodeIdentity snId) {
    super(RequestType.UpdateLeftNode);
    this.level = level;
    this.snId = snId;
  }
}
