package network.underlay.packets.requests;

import network.underlay.packets.Request;
import network.underlay.packets.RequestType;

/**
 * Represents a request that is used to check whether the node that is being queried is currently
 * available (i.e. is inserted.) If not, then the search should not be routed through this node.
 */
public class IsAvailableRequest extends Request {

  public IsAvailableRequest() {
    super(RequestType.IsAvailable);
  }
}
