package underlay.packets.requests;

import underlay.packets.Request;
import underlay.packets.RequestType;

/** Request for getting right node. */
public class GetRightNodeRequest extends Request {

  public final int level;

  public GetRightNodeRequest(int level) {
    super(RequestType.GetRightNode);
    this.level = level;
  }
}
