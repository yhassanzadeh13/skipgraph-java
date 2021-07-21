package underlay.packets.requests;

import underlay.packets.Request;
import underlay.packets.RequestType;

public class NameIDLevelSearchRequest extends Request {

  public final int level;
  public final int direction;
  public final String targetNameID;

  public NameIDLevelSearchRequest(int level, int direction, String targetNameID) {
    super(RequestType.NameIDLevelSearch);
    this.level = level;
    this.direction = direction;
    this.targetNameID = targetNameID;
  }
}
