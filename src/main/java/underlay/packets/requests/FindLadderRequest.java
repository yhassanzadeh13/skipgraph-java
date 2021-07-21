package underlay.packets.requests;

import underlay.packets.Request;
import underlay.packets.RequestType;

public class FindLadderRequest extends Request {

  public final int level;
  public final int direction;
  public final String target;

  public FindLadderRequest(int level, int direction, String target) {
    super(RequestType.FindLadder);
    this.level = level;
    this.direction = direction;
    this.target = target;
  }
}
