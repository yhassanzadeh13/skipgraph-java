package underlay.tcp;

import underlay.RequestHandler;
import underlay.packets.ResponseParameters;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * Represents a thread that handles an incoming TCP request and emits a response.
 */
public class TCPHandler implements Runnable {

    // TCP connection. We use this connection to read the request and send back the response.
    private final Socket incomingConnection;
    // Request handler.
    private final RequestHandler requestHandler;

    public TCPHandler(Socket incomingConnection, RequestHandler requestHandler) {
        this.incomingConnection = incomingConnection;
        this.requestHandler = requestHandler;
    }

    @Override
    public void run() {
        ObjectInputStream requestStream;
        ObjectOutputStream responseStream;
        // Construct the streams from the connection.
        try {
            requestStream = new ObjectInputStream(incomingConnection.getInputStream());
            responseStream = new ObjectOutputStream(incomingConnection.getOutputStream());
        } catch (IOException e) {
            System.err.println("[TCPHandler] Could not acquire the streams from the connection.");
            e.printStackTrace();
            return;

        }
        // Read the request from the connection.
        TCPRequest request;
        try {
            request = (TCPRequest) requestStream.readObject();
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("[TCPHandler] Could not read the request.");
            e.printStackTrace();
            return;
        }
        // Acquire the response.
        ResponseParameters responseParameters = requestHandler.dispatchRequest(request.type, request.parameters);
        // Write the response to the connection.
        try {
            responseStream.writeObject(new TCPResponse(responseParameters));
        } catch (IOException e) {
            System.err.println("[TCPHandler] Could not send the response.");
            e.printStackTrace();
            return;
        }
        // Close the connection & streams.
        try {
            requestStream.close();
            responseStream.close();
            incomingConnection.close();
        } catch (IOException e) {
            System.err.println("[TCPHandler] Could not close the incoming connection.");
            e.printStackTrace();
        }
    }
}
