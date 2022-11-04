package underlay;

import java.net.Inet4Address;
import java.net.UnknownHostException;

import middlelayer.MiddleLayer;
import underlay.packets.Request;
import underlay.packets.Response;
import underlay.tcp.TcpUnderlay;

/**
 * Represents the underlay layer of the skip-graph DHT. Handles node-to-node communication.
 */
public abstract class Underlay {

  private MiddleLayer middleLayer;

  private int port;
  private String address;
  private String fullAddress;

  /**
   * Constructs a new default underlay. Must be initialized and connected to the middle layer.
   *
   * @return a new default underlay.
   */
  public static Underlay newDefaultUnderlay() {
    return new TcpUnderlay();
  }

  public void setMiddleLayer(MiddleLayer middleLayer) {
    this.middleLayer = middleLayer;
  }

  public int getPort() {
    return port;
  }

  public String getAddress() {
    return address;
  }

  public String getFullAddress() {
    return fullAddress;
  }

  /**
   * Dispatches a request to the middle layer and returns the response.
   *
   * @param request the request.
   * @return emitted response.
   */
  public Response dispatchRequest(Request request) {
    return middleLayer.receive(request);
  }

  /**
   * Initializes the underlay.
   *
   * @param port the port that the underlay should be bound to.
   * @return true iff the initialization was successful.
   */
  public final boolean initialize(int port) {
    port = initUnderlay(port);
    if (port < 0) {
      throw new IllegalArgumentException("port must be non-negative:" + port);
    }

    this.port = port;
    try {
      address = Inet4Address.getLocalHost().getHostAddress();
    } catch (UnknownHostException e) {
      throw new IllegalStateException("could not get local host address.", e);
    }
    fullAddress = address + ":" + port;
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
   * @param address address of the remote server.
   * @param port    port of the remote server.
   * @param request the request.
   * @return response emitted by the remote server.
   */
  public abstract Response sendMessage(String address, int port, Request request);

  /**
   * Terminates the underlay.
   *
   * @return true iff the termination was successful.
   */
  public abstract boolean terminate();
}
