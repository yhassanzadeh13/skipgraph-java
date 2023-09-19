package underlay.packets.requests;

import model.identifier.Identity;
import model.skipgraph.Direction;
import underlay.packets.Request;
import underlay.packets.RequestType;

/**
 * UpdateLookupTableNeighborRequest is a request for a remote node to update its lookup table with a new neighbor.
 */
public class UpdateLookupTableNeighborRequest extends Request {
  /**
   * The level to be written at lookupTable[level][direction].
   */
  public final int level;
  /**
   * The direction to be written at lookupTable[level][direction].
   */
  public final Direction direction;
  /**
   * The node identity to be written at lookupTable[level][direction], i.e., the new neighbor.
   */
  public final Identity identity;

  /**
   * Constructor for UpdateLookupTableNeighborRequest.
   *
   * @param level    The level to be written at lookupTable[level][direction].
   * @param direction The direction to be written at lookupTable[level][direction].
   * @param identity The node identity to be written at lookupTable[level][direction].
   */
  public UpdateLookupTableNeighborRequest(int level, Direction direction, Identity identity) {
    super(RequestType.UpdateLookupTableNeighbor);
    this.level = level;
    this.direction = direction;
    this.identity = identity;
  }
}
