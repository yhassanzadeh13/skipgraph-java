package network.underlay.packets.requests;

import network.underlay.packets.Request;
import network.underlay.packets.RequestType;

/** Request for getting left node. */
public class GetLeftNodeRequest extends Request {

  public final int level;

  public GetLeftNodeRequest(int level) {
    super(RequestType.GetLeftNode);
    this.level = level;
  }
}
