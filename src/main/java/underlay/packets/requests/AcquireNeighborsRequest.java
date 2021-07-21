package underlay.packets.requests;

import skipnode.SkipNodeIdentity;
import underlay.packets.Request;
import underlay.packets.RequestType;

public class AcquireNeighborsRequest extends Request {

  public final SkipNodeIdentity newNode;
  public final int level;

  public AcquireNeighborsRequest(SkipNodeIdentity newNode, int level) {
    super(RequestType.AcquireNeighbors);
    this.newNode = newNode;
    this.level = level;
  }
}
