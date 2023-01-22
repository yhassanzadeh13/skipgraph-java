package underlay.udp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

import underlay.packets.Request;
import underlay.packets.Response;

/**
 * Implements a routine that continuously listens a local UDP port for requests and responses, and
 * delegates the handling of each received request to a `UDPHandler` thread and each received
 * response to the main `UDPUnderlay` thread.
 */
public class UdpListener implements Runnable {

  // Owned resource by the `UDPUnderlay`.
  private final DatagramSocket listenSocket;
  // Owned resource by the `UDPUnderlay`.
  private final UdpUnderlay underlay;
  // Owned resource by the `UDPUnderlay`. Used to dispatch the received
  // responses to the main thread.
  private final UdpResponseLock responseLock;

  /**
   * Constructor for UdpListener.
   *
   * @param listenSocket listener socket.
   * @param underlay     UDP underlay instance.
   * @param responseLock response lock.
   */
  public UdpListener(DatagramSocket listenSocket, UdpUnderlay underlay, UdpResponseLock responseLock) {
    this.listenSocket = listenSocket;
    this.underlay = underlay;
    this.responseLock = responseLock;
  }

  @Override
  public void run() {
    while (true) {
      try {
        // Allocate the size for a packet.
        byte[] packetBytes = new byte[UdpUnderlay.MAX_PACKET_SIZE];
        DatagramPacket packet = new DatagramPacket(packetBytes, packetBytes.length);
        // Wait for a packet.
        listenSocket.receive(packet);
        // Deserialize the packet.
        Object packetObject = UdpUtils.deserialize(packet.getData(), packet.getLength());
        // If the packet is a request, handle it in a new `UDPHandler` thread.
        if (packetObject instanceof Request) {
          Request request = (Request) packetObject;
          new Thread(new UdpHandler(listenSocket, request, packet.getAddress(), packet.getPort(), underlay)).start();
        } else if (packetObject instanceof Response) {
          // If the packet is a response, dispatch the response to the main thread.
          responseLock.dispatch((Response) packetObject);
        } else {
          System.err.println("[UDPListener] Could not parse the received packet.");
        }
        // TODO: manage the termination of the handler threads.

      } catch (SocketException e) {
        // Once the listener socket is closed by an outside thread, this point will be reached and
        // we will stop listening.
        return;
      } catch (IOException e) {
        throw new IllegalStateException("could not receive UDP packet.", e);
      }
    }
  }
}
