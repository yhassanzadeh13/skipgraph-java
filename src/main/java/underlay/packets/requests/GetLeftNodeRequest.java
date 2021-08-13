package underlay.packets.requests;

import underlay.packets.Request;
import underlay.packets.RequestType;

/** Request for getting left node. */
public class GetLeftNodeRequest extends Request {

  public final int level;

  public GetLeftNodeRequest(int level) {
    super(RequestType.GetLeftNode);
    this.level = level;
  }
}
