package network.packets.requests;

import network.packets.Request;
import network.packets.RequestType;

/** Request for searching by num id. */
public class SearchByNumIdRequest extends Request {

  public final int targetNumId;

  public SearchByNumIdRequest(int targetNumId) {
    super(RequestType.SearchByNumId);
    this.targetNumId = targetNumId;
  }
}
