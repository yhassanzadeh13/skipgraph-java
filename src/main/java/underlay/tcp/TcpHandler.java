package underlay.tcp;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import underlay.packets.Request;
import underlay.packets.Response;

/**
 * Represents a thread that handles an incoming TCP request and emits a response.
 */
public class TcpHandler implements Runnable {

  // TCP stream. We use this two-way stream to read the request and send back the response.
  private final Socket incomingConnection;
  // TCP underlay.
  private final TcpUnderlay underlay;

  public TcpHandler(Socket incomingConnection, TcpUnderlay underlay) {
    this.incomingConnection = incomingConnection;
    this.underlay = underlay;
  }

  // TODO send back an error response when necessary.
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
    Request request;
    try {
      request = (Request) requestStream.readObject();
    } catch (IOException | ClassNotFoundException e) {
      System.err.println("[TCPHandler] Could not read the request.");
      e.printStackTrace();
      return;
    }
    // Acquire the response.
    Response responseParameters = underlay.dispatchRequest(request);
    // Write the response to the connection.
    try {
      responseStream.writeObject(responseParameters);
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
