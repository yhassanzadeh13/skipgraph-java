package network.javarmi;

import java.rmi.Remote;
import java.rmi.RemoteException;

import network.packets.Request;
import network.packets.Response;

/**
 * Represents a Java RMI Service. A RMI service only has a single function that dispatches the
 * received request to the local `RequestHandler` instance.
 */
public interface JavaRmiService extends Remote {

  Response handleRequest(Request request) throws RemoteException;
}
