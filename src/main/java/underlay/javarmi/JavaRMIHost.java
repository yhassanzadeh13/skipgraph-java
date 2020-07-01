package underlay.javarmi;

import underlay.packets.RequestParameters;
import underlay.packets.RequestType;
import underlay.packets.ResponseParameters;

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
    public ResponseParameters handleRequest(RequestType type, RequestParameters parameters) {
        return underlay.dispatchRequest(type, parameters);
    }
}
