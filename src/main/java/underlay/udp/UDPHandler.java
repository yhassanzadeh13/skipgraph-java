package underlay.udp;

import underlay.RequestHandler;
import underlay.packets.RequestPacket;
import underlay.packets.ResponseParameters;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * Represents a thread that handles a UDP request and emits a response.
 */
public class UDPHandler implements Runnable {

    // The UDP socket that the response will be sent through.
    private final DatagramSocket udpSocket;
    // The received request to handle.
    private final RequestPacket request;
    // The address of the client that the request was sent from.
    private final InetAddress clientAddress;
    // The port of the client that the request was sent from.
    private final int clientPort;
    // The handler which will be handling this request.
    private final RequestHandler requestHandler;

    public UDPHandler(DatagramSocket udpSocket, RequestPacket request, InetAddress clientAddress, int clientPort,
                      RequestHandler requestHandler) {
        this.udpSocket = udpSocket;
        this.request = request;
        this.clientAddress = clientAddress;
        this.clientPort = clientPort;
        this.requestHandler = requestHandler;
    }

    // TODO send back an error response when necessary.
    @Override
    public void run() {
        ResponseParameters response = requestHandler.dispatchRequest(request.type, request.parameters);
        // Serialize the response.
        byte[] responseBytes = UDPUtils.serialize(response);
        if(responseBytes == null) {
            System.err.println("[UDPHandler] Invalid response.");
            return;
        }
        // Construct the response packet.
        DatagramPacket responsePacket = new DatagramPacket(responseBytes, responseBytes.length, clientAddress, clientPort);
        // Send the response packet.
        try {
            udpSocket.send(responsePacket);
        } catch (IOException e) {
            System.err.println("[UDPHandler] Could not send the response.");
            e.printStackTrace();
        }
    }
}
