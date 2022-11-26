package unittest;

import java.util.ArrayList;
import java.util.concurrent.locks.ReentrantLock;

import underlay.Underlay;
import underlay.packets.Request;
import underlay.packets.Response;

public class NetworkHub {
  private final ArrayList<Underlay> underlays;

  public NetworkHub() {
    underlays = new ArrayList<>();
  }

  public void addUnderlay(MockUnderlay underlay) {
    underlays.add(underlay);
  }

  public Response deliver(String address, int port, Request request) throws IllegalStateException {
    for (Underlay underlay : underlays) {
      if (underlay.getAddress().equals(address) && underlay.getPort() == port) {
        return underlay.dispatchRequest(request);
      }
    }

    throw new IllegalStateException("No underlay found for address " + address + " and port " + port);
  }

  public boolean removeUnderlay(Underlay underlay) {
    return underlays.remove(underlay);
  }

  public void printUnderlays() {
    for (Underlay underlay : underlays) {
      System.out.println(underlay.getFullAddress());
    }
  }
}
