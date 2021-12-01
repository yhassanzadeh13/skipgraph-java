package network.underlay.packets.requests;

import network.underlay.packets.Request;
import network.underlay.packets.RequestType;
import skipnode.SkipNodeIdentity;

/**
 * Request for announcing a neighbour.
 */
public class AnnounceNeighborRequest extends Request {

  public final SkipNodeIdentity newNeighbor;
  public final int minLevel;

  /**
   * Constructor for AnnounceNeighborRequest.
   *
   * @param newNeighbor Skipnode that is the new neighbour.
   * @param minLevel    Integer representing the minimum level.
   */
  public AnnounceNeighborRequest(SkipNodeIdentity newNeighbor, int minLevel) {
    super(RequestType.AnnounceNeighbor);
    this.newNeighbor = newNeighbor;
    this.minLevel = minLevel;
  }
}
