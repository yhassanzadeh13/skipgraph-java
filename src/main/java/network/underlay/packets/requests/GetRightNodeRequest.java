package network.underlay.packets.requests;

import network.underlay.packets.Request;
import network.underlay.packets.RequestType;

/** Request for getting right node. */
public class GetRightNodeRequest extends Request {

  public final int level;

  public GetRightNodeRequest(int level) {
    super(RequestType.GetRightNode);
    this.level = level;
  }
}
