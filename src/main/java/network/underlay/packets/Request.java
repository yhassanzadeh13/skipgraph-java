package network.underlay.packets;

import java.io.Serializable;

import model.Address;

/** Represents a serializable request packet. Every request type must inherit from this class. */
public class Request implements Serializable {
  public final RequestType type;

  // TODO: can we only send an id?
  private Address originAddress;

  // ID of the receiver, -1 denotes the request is to the master node
  public int receiverId = -1;

  // TODO: backoff should not be determined by application layer.
  // Denotes whether the middle layer should keep trying to deliver the request to a locked overlay
  // at the client. If this is set to false, the overlay needs to check whether the response is a
  // `locked` response and act accordingly.
  public boolean backoff = true;

  public Request(RequestType type) {
    this.type = type;
  }

  public Address getOriginAddress() {
    return originAddress;
  }

  public void setOriginAddress(Address originAddress) {
    this.originAddress = originAddress;
  }
}
