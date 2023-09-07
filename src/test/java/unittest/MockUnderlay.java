package unittest;

import java.util.Random;

import underlay.Underlay;
import underlay.packets.Request;
import underlay.packets.Response;

public class MockUnderlay extends Underlay {
  private final NetworkHub networkHub;
  private static final Random random = new Random();

  public MockUnderlay(NetworkHub networkHub) {
    this.networkHub = networkHub;
    this.networkHub.addUnderlay(this);
  }

  @Override
  protected int initUnderlay(int port) {
    if(port <= 0) {
      port = random.nextInt(10000) + 10000;
    }
    return port;
  }

  @Override
  public Response sendMessage(String address, int port, Request request) {
    return this.networkHub.deliver(address, port, request);
  }

  @Override
  public boolean terminate() {
    return this.networkHub.removeUnderlay(this);
  }
}
