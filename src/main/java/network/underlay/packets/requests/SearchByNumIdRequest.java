package network.underlay.packets.requests;

import network.underlay.packets.Request;
import network.underlay.packets.RequestType;

/** Request for searching by num id. */
public class SearchByNumIdRequest extends Request {

  public final int targetNumId;

  public SearchByNumIdRequest(int targetNumId) {
    super(RequestType.SearchByNumId);
    this.targetNumId = targetNumId;
  }
}
