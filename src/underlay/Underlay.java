package underlay;

import underlay.javarmi.JavaRMIAdapter;
import underlay.packets.RequestParameters;
import underlay.packets.RequestType;
import underlay.packets.ResponseParameters;

import java.rmi.RemoteException;

/**
 * Represents the underlay layer of the skip-graph DHT. Handles node-to-node communication.
 */
public class Underlay {

    private ConnectionAdapter connectionAdapter;

    /**
     * Creates and returns a new connection adapter.
     * @return a new connection adapter.
     */
    private static ConnectionAdapter defaultAdapter() throws RemoteException {
        return new JavaRMIAdapter();
    }

    /**
     * Constructs the underlay.
     * @param port the port that the adapter should be bound to.
     */
    public Underlay(int port) {
        // Initialize & register the underlay connection adapter.
        try {
            ConnectionAdapter adapter = Underlay.defaultAdapter();
            if(adapter.construct(port)) connectionAdapter = adapter;
        } catch (RemoteException e) {
            System.err.println("[Underlay] Error while initializing the underlay.");
            e.printStackTrace();
        }
    }

    /**
     * Can be used to send a message to a remote server that runs the same underlay architecture.
     * @param address address of the remote server.
     * @param t type of the request.
     * @param p parameters of the request.
     * @return response emitted by the remote server.
     */
    public ResponseParameters sendMessage(String address, RequestType t, RequestParameters p) {
        if(connectionAdapter == null) {
            System.err.println("[Underlay] Adapter does not exist.");
            return null;
        }
        // Connect to the remote adapter.
        ConnectionAdapter remote = null;
        try {
            remote = connectionAdapter.remote(address);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        if(remote == null) {
            System.err.println("[Underlay] Could not send the message.");
            return null;
        }
        // Transform the request to RMI invocations.
        try {
            switch (t) {
                case SearchByNameID:
                    return remote.searchByNameID((String) p.getRequestValue("targetNameID"));
                case SearchByNumID:
                    return remote.searchByNumID((Integer) p.getRequestValue("targetNumID"));
                case NameIDLevelSearch:
                    return remote.nameIDLevelSearch((Integer) p.getRequestValue("level"),
                            (String) p.getRequestValue("targetNameID"));
                case UpdateLeftNode:
                    return remote.updateLeftNode((Integer) p.getRequestValue("level"),
                            (String) p.getRequestValue("newValue"));
                case UpdateRightNode:
                    return remote.updateRightNode((Integer) p.getRequestValue("level"),
                            (String) p.getRequestValue("newValue"));
            }
        } catch (Exception e) {
            System.err.println();
            return null;
        }
        return null;
    }
}
