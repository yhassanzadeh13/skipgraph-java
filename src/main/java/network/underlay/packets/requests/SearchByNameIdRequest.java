package network.underlay.packets.requests;

import network.underlay.packets.Request;
import network.underlay.packets.RequestType;

/** Request for searching by name id. */
public class SearchByNameIdRequest extends Request {

  public final String targetNameId;

  public SearchByNameIdRequest(String targetNameId) {
    super(RequestType.SearchByNameId);
    this.targetNameId = targetNameId;
  }
}
