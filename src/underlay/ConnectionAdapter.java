package underlay;

import underlay.packets.ResponseParameters;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Represents an interface for an RPC adapter. RPC mechanisms can be implemented by implementing this interface and
 * setting it as the adapter in the appropriate `Underlay` object.
 */
public interface ConnectionAdapter extends Remote {

    // Initializes the adapter on the host machine at the given port.
    boolean construct(int port) throws RemoteException;
    // Terminates the adapter service on the host machine.
    void destruct() throws RemoteException;
    // Connects to the remote machine's adapter.
    ConnectionAdapter remote(String address) throws RemoteException;

    /**
     * We require each RPC client to be able to handle these skip-graph primitives.
     */
    // Returns the address of the RPC client.
    String getAddress() throws RemoteException;
    // Performs a name ID search from this client.
    ResponseParameters searchByNameID(String targetNameID) throws RemoteException;
    // Performs a numerical ID search from this client.
    ResponseParameters searchByNumID(int targetNumID) throws RemoteException;
    // Performs a name ID search from this client at the given level.
    ResponseParameters nameIDLevelSearch(int level, String targetNameID) throws RemoteException;
    // Updates the left neighbor of this node at the given level with the new given value.
    ResponseParameters updateLeftNode(int level, String newValue) throws RemoteException;
    // Updates the right neighbor of this node at the given level with the new given value.
    ResponseParameters updateRightNode(int level, String newValue) throws RemoteException;
}
