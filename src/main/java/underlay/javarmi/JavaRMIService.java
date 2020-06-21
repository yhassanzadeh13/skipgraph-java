package underlay.javarmi;

import underlay.packets.RequestParameters;
import underlay.packets.RequestType;
import underlay.packets.ResponseParameters;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Represents a Java RMI Service. A RMI service only has a single function that dispatches the received request
 * to the local `RequestHandler` instance.
 */
public interface JavaRMIService extends Remote {
    ResponseParameters handleRequest(RequestType type, RequestParameters parameters) throws RemoteException;
}
