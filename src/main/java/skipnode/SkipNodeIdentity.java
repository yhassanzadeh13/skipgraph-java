package skipnode;

import java.io.Serializable;
import java.util.Objects;

import log.Log4jLogger;
import model.identifier.Identifier;
import model.identifier.MembershipVector;
import org.apache.logging.log4j.LogManager;

/**
 * Basic skipnode.SkipNodeIdentity class
 */
public class SkipNodeIdentity implements Serializable {
  private final MembershipVector membershipVector; // aka name Id
  private final Identifier identifier; // aka numerical id
  private final String address;
  private final int port;

  /**
   * Constructor for SkipNodeIdentity.
   *
   * @param identifier identifier of the node (aka numerical ID).
   * @param membershipVector membership vector of the node (aka name ID).
   * @param address String representing the address of the node.
   * @param port    Integer representing the port of the node.
   */
  public SkipNodeIdentity(Identifier identifier, MembershipVector membershipVector, String address, int port) {
    this.membershipVector = membershipVector;
    this.identifier = identifier;
    this.address = address;
    this.port = port;
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

  public MembershipVector getMembershipVector() {
    return membershipVector;
  }

  public Identifier getIdentifier() {
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
    return "identifier: "
        + identifier.toString()
        + "\tmembership vector: "
        + membershipVector.toString()
        + "\taddress: "
        + address
        + "\tport: "
        + port;
  }
}
