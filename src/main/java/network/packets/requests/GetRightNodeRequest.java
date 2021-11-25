package network.packets.requests;

import network.packets.Request;
import network.packets.RequestType;

/** Request for getting right node. */
public class GetRightNodeRequest extends Request {

  public final int level;

  public GetRightNodeRequest(int level) {
    super(RequestType.GetRightNode);
    this.level = level;
  }
}
