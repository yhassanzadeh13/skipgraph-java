package network;

import java.net.Inet4Address;
import java.net.UnknownHostException;

import model.Address;
import network.underlay.packets.Request;
import network.underlay.packets.Response;
import network.underlay.tcp.TcpUnderlay;

/**
 * Represents the underlay layer of the skip-graph DHT. Handles node-to-node communication.
 */
public abstract class Underlay {

  private Network network;
  private Address address;


  /**
   * Constructs a new default underlay. Must be initialized and connected to the middle layer.
   *
   * @return a new default underlay.
   */
  public static Underlay newDefaultUnderlay() {
    return new TcpUnderlay();
  }

  public void setMiddleLayer(Network network) {
    this.network = network;
  }

  public Address getAddress() {
    return address;
  }

  /**
   * Dispatches a request to the middle layer and returns the response.
   *
   * @param request the request.
   * @return emitted response.
   */
  public Response dispatchRequest(Request request) {
    return network.receive(request);
  }

  /**
   * Initializes the underlay.
   *
   * @param port the port that the underlay should be bound to.
   * @return true iff the initialization was successful.
   */
  public final boolean initialize(int port) {
    String ip;

    port = initUnderlay(port);
    if (port <= 0) {
      return false;
    }

    try {
      ip = Inet4Address.getLocalHost().getHostAddress();
      this.address = new Address(ip, port);
    } catch (UnknownHostException e) {
      System.err.println("[Underlay] Could not acquire the local host name during initialization.");
      e.printStackTrace();
      return false;
    }

    return true;
  }

  /**
   * Contains the underlay-specific initialization procedures.
   *
   * @param port the port that the underlay should be bound to.
   * @return port number underlay initialized on or -1 if initialization is unsuccessful.
   */
  protected abstract int initUnderlay(int port);

  /**
   * Can be used to send a request to a remote server that runs the same underlay architecture.
   *
   * @param dst address of the remote server.
   * @param request the request.
   * @return response emitted by the remote server.
   */
  public abstract Response sendMessage(Address dst, Request request);

  /**
   * Terminates the underlay.
   *
   * @return true iff the termination was successful.
   */
  public abstract boolean terminate();
}
