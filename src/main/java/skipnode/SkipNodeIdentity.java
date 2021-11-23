package skipnode;

import java.io.Serializable;
import java.util.Objects;

import log.Log4jLogger;
import org.apache.logging.log4j.LogManager;

/**
 * Basic skipnode.SkipNodeIdentity class
 */
public class SkipNodeIdentity implements Serializable, Comparable<SkipNodeIdentity> {

  private static final Log4jLogger logger =
      new Log4jLogger(LogManager.getLogger(SkipNodeIdentity.class));
  private final String membershipVector;
  private final int identifier;
  private final String address;
  private final int port;
  // Denotes the lookup table version.
  public int version;

  /**
   * Constructor for SkipNodeIdentity.
   *
   * @param nameId  name id of the node.
   * @param numId   numerical id of the node.
   * @param address String representing the address of the node.
   * @param port    Integer representing the port of the node.
   * @param version Integer representing the vversion.
   */
  public SkipNodeIdentity(String nameId, int numId, String address, int port, int version) {
    this.membershipVector = nameId;
    this.identifier = numId;
    this.address = address;
    this.port = port;
    this.version = version;
  }

  public SkipNodeIdentity(String nameId, int numId, String address, int port) {
    this(nameId, numId, address, port, 0);
  }

  /**
   * Method that calculates common bits for 2 name ids.
   *
   * @param name1 String representing name id.
   * @param name2 String representing name id.
   * @return number of common bits.
   */
  public static int commonBits(String name1, String name2) {
    if (name1 == null || name2 == null) {
      return -1;
    }
    if (name1.length() != name2.length()) {
      return -1;
    }
    int i = 0;
    while (i < name1.length() && name1.charAt(i) == name2.charAt(i)) {
      i++;
    }
    return i;
  }

  public String getMembershipVector() {
    return membershipVector;
  }

  public int getIdentifier() {
    return identifier;
  }

  public String getAddress() {
    return address;
  }

  public int getPort() {
    return port;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    SkipNodeIdentity that = (SkipNodeIdentity) o;
    return getIdentifier() == that.getIdentifier()
        && getMembershipVector().equals(that.getMembershipVector())
        && getAddress().equals(that.getAddress())
        && getPort() == that.getPort();
  }

  @Override
  public int hashCode() {
    return Objects.hash(getMembershipVector(), getIdentifier(), getAddress(), getPort());
  }

  @Override
  public String toString() {
    return "Name ID: "
        + membershipVector
        + "\tNum ID: "
        + identifier
        + "\tAddress: "
        + address
        + "\tPort: "
        + port;
  }

  @Override
  public int compareTo(SkipNodeIdentity o) {
    logger
        .debug()
        .addInt("num_id", this.identifier)
        .addInt("compared_num_id", o.getIdentifier())
        .addMsg("currently comparing");
    return Integer.compare(identifier, o.identifier);
  }
}
