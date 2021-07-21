package underlay.javarmi;

import underlay.packets.Request;
import underlay.packets.RequestType;
import underlay.packets.Response;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

/**
 * Represents the Java RMI Service implementation.
 */
public class JavaRMIHost extends UnicastRemoteObject implements JavaRMIService {

  private final JavaRMIUnderlay underlay;

  public JavaRMIHost(JavaRMIUnderlay underlay) throws RemoteException {
    this.underlay = underlay;
  }

  @Override
  public Response handleRequest(Request request) {
    return underlay.dispatchRequest(request);
  }
}
