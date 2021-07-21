package underlay.packets;

import java.io.Serializable;

/**
 * Represents a serializable request packet. Every request type must inherit from this class.
 */
public class Request implements Serializable {

  public final RequestType type;
  public String senderAddress;
  public int senderPort;
  // ID of the receiver, -1 denotes the request is to the master node
  public int receiverID = -1;
  // Denotes whether the middle layer should keep trying to deliver the request to a locked overlay
  // at the client. If this is set to false, the overlay needs to check whether the response is a
  // `locked` response and act accordingly.
  public boolean backoff = true;

  public Request(RequestType type) {
    this.type = type;
  }
}
