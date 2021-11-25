package network.packets.requests;

import network.packets.Request;
import network.packets.RequestType;

/** Request for getting left node. */
public class GetLeftNodeRequest extends Request {

  public final int level;

  public GetLeftNodeRequest(int level) {
    super(RequestType.GetLeftNode);
    this.level = level;
  }
}
