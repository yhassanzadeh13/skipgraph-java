package underlay.javarmi;

import underlay.RequestHandler;
import underlay.packets.RequestParameters;
import underlay.packets.RequestType;
import underlay.packets.ResponseParameters;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

/**
 * Represents the Java RMI Service implementation.
 */
public class JavaRMIHost extends UnicastRemoteObject implements JavaRMIService {

    private final RequestHandler requestHandler;

    public JavaRMIHost(RequestHandler requestHandler) throws RemoteException {
        this.requestHandler = requestHandler;
    }

    @Override
    public ResponseParameters handleRequest(RequestType type, RequestParameters parameters) {
        return requestHandler.dispatchRequest(type, parameters);
    }
}
