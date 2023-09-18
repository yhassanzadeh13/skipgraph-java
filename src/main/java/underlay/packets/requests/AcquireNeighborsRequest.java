package underlay.packets.requests;

import model.identifier.Identity;
import underlay.packets.Request;
import underlay.packets.RequestType;

/**
 * Request for acquiring neighbour.
 */
public class AcquireNeighborsRequest extends Request {

  public final Identity newNode;
  public final int level;

  /**
   * Constructor for AcquireNeighborsRequest.
   *
   * @param newNode node that wants to acquire the neighbour.
   * @param level   Integer representing the level.
   */
  public AcquireNeighborsRequest(Identity newNode, int level) {
    super(RequestType.AcquireNeighbors);
    this.newNode = newNode;
    this.level = level;
  }
}
