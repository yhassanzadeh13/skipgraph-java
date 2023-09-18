package underlay.packets.requests;

import skipnode.Identity;
import underlay.packets.Request;
import underlay.packets.RequestType;

/**
 * Request for announcing a neighbour.
 */
public class AnnounceNeighborRequest extends Request {

  public final Identity newNeighbor;
  public final int minLevel;

  /**
   * Constructor for AnnounceNeighborRequest.
   *
   * @param newNeighbor Skipnode that is the new neighbour.
   * @param minLevel    Integer representing the minimum level.
   */
  public AnnounceNeighborRequest(Identity newNeighbor, int minLevel) {
    super(RequestType.AnnounceNeighbor);
    this.newNeighbor = newNeighbor;
    this.minLevel = minLevel;
  }
}
