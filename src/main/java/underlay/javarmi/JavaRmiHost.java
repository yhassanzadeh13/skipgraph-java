package underlay.javarmi;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import underlay.packets.Request;
import underlay.packets.Response;

/**
 * Represents the Java RMI Service implementation.
 */
public class JavaRmiHost extends UnicastRemoteObject implements JavaRmiService {

  private final JavaRmiUnderlay underlay;

  public JavaRmiHost(JavaRmiUnderlay underlay) throws RemoteException {
    this.underlay = underlay;
  }

  @Override
  public Response handleRequest(Request request) {
    return underlay.dispatchRequest(request);
  }
}
