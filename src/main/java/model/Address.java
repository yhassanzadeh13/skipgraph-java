package model;

import java.io.Serializable;
import java.util.Objects;

/**
 * Represents the network address of a skip graph node.
 */
public class Address implements Serializable {
  private final String ip;
  private final int port;

  public Address(String ip, int port) {
    this.ip = ip;
    this.port = port;
  }

  public String getIp() {
    return ip;
  }

  public int getPort() {
    return port;
  }

  @Override
  public String toString() {
    return this.ip + ":" + this.port;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Address)) {
      return false;
    }
    Address address = (Address) o;
    return getPort() == address.getPort() && getIp().equals(address.getIp());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getIp(), getPort());
  }
}
