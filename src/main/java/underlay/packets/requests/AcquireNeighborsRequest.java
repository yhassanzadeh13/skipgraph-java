package underlay.packets.requests;

import skipnode.SkipNodeIdentity;
import underlay.packets.Request;
import underlay.packets.RequestType;

/**
 * Request for acquiring neighbour.
 */
public class AcquireNeighborsRequest extends Request {

  public final SkipNodeIdentity newNode;
  public final int level;

  /**
   * Constructor for AcquireNeighborsRequest.
   *
   * @param newNode node that wants to acquire the neighbour.
   * @param level Integer representing the level.
   */
  public AcquireNeighborsRequest(SkipNodeIdentity newNode, int level) {
    super(RequestType.AcquireNeighbors);
    this.newNode = newNode;
    this.level = level;
  }
}
