package network.underlay.javarmi;

import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.ExportException;

import network.Underlay;
import network.underlay.packets.Request;
import network.underlay.packets.Response;

/**
 * Java RMI connection underlay implementation.
 */
public class JavaRmiUnderlay extends Underlay {

  // Java RMI instance running at the host machine.
  JavaRmiHost host;

  /**
   * Connects to the Java RMI adapter of a remote server.
   *
   * @param fullAddress address of the server in the form of IP:PORT
   * @return a remote Java RMI adapter.
   */
  public JavaRmiService remote(String fullAddress) {
    if (host == null) {
      System.err.println("[JavaRMIUnderlay] Host does not exist.");
      return null;
    }
    JavaRmiService remote;
    try {
      remote = (JavaRmiService) Naming.lookup("//" + fullAddress + "/node");
    } catch (Exception e) {
      System.err.println("[JavaRMIUnderlay] Could not connect to the remote RMI server!");
      return null;
    }
    return remote;
  }

  /**
   * Constructs a `JavaRMIHost` instance and binds it to the given port.
   *
   * @param port the port that the underlay should be bound to.
   * @return port number underlay initialized on or -1 if initialization is unsuccessful.
   */
  @Override
  protected int initUnderlay(int port) {
    try {
      host = new JavaRmiHost(this);
      // Bind this RMI adapter to the given port.
      LocateRegistry.createRegistry(port).rebind("node", host);

    } catch (ExportException e) {
      port = (port + 1) % 60000; // tries another port in this range.
      return initUnderlay(port);

    } catch (Exception e) {
      System.err.println("[JavaRMIUnderlay] Error while initializing at port " + port);
      e.printStackTrace();
      return -1;
    }

    return port;
  }

  /**
   * Invokes the appropriate RMI method on the server with the given address.
   *
   * @param address address of the remote server.
   * @param port    port of the remote server.
   * @param request request to send.
   * @return response received from the server.
   */
  @Override
  public Response sendMessage(String address, int port, Request request) {
    if (host == null) {
      System.err.println("[JavaRMIUnderlay] Host does not exist.");
      return null;
    }
    // Connect to the remote adapter.
    JavaRmiService remote = remote(address + ":" + port);
    if (remote == null) {
      System.err.println(
          "[JavaRMIUnderlay] Could not connect to the address: " + address + ":" + port);
      return null;
    }
    // Use the remote handler to dispatch the request.
    try {
      return remote.handleRequest(request);
    } catch (Exception e) {
      System.err.println("[JavaRMIUnderlay] Could not send the message.");
      e.printStackTrace();
      return null;
    }
  }

  /**
   * Terminates the Java RMI underlay service.
   */
  @Override
  public boolean terminate() {
    try {
      Naming.unbind("//" + this.getAddress().toString() + "/node");
    } catch (Exception e) {
      System.err.println("[JavaRMIUnderlay] Could not terminate.");
      e.printStackTrace();
      return false;
    }
    return true;
  }
}
