package underlay.packets.requests;

import model.identifier.Identifier;
import underlay.packets.Request;
import underlay.packets.RequestType;

/**
 * Request object encapsulating a search by identifier (aka search by numerical id).
 */
public class SearchByIdentifierRequest extends Request {
  public final Identifier targetNumId;

  public SearchByIdentifierRequest(Identifier targetNumId) {
    super(RequestType.SearchByNumId);
    this.targetNumId = targetNumId;
  }
}
