package network.packets.requests;

import network.packets.Request;
import network.packets.RequestType;

/** Request for searching by name id recursively. */
public class SearchByNameIdRecursiveRequest extends Request {

  public final String target;
  public final int level;

  /**
   * Constructor for SearchByNameIdRecursiveRequest.
   *
   * @param target name id of the target node.
   * @param level Integer representing the level.
   */
  public SearchByNameIdRecursiveRequest(String target, int level) {
    super(RequestType.SearchByNameIDRecursive);
    this.target = target;
    this.level = level;
  }
}
