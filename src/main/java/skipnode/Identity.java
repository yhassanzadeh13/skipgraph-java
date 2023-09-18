package skipnode;

import java.io.Serializable;
import java.util.Objects;

import static lookup.LookupTable.EMPTY_NODE;

import model.identifier.Identifier;
import model.identifier.MembershipVector;

/**
 * Identity of a node in the skip graph; it contains the membership vector, the identifier, the address and the port of the node.
 */
public class Identity implements Serializable {
  private final MembershipVector membershipVector; // aka name Id
  private final Identifier identifier; // aka numerical id
  private final String address;
  private final int port;

  /**
   * Constructor for Identity.
   *
   * @param identifier       identifier of the node (aka numerical ID).
   * @param membershipVector membership vector of the node (aka name ID).
   * @param address          String representing the address of the node.
   * @param port             Integer representing the port of the node.
   */
  public Identity(Identifier identifier, MembershipVector membershipVector, String address, int port) {
    this.membershipVector = membershipVector;
    this.identifier = identifier;
    this.address = address;
    this.port = port;
  }

  public MembershipVector getMemVec() {
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
    Identity that = (Identity) o;
    if (this.port != that.port) {
      return false;
    }
    if (!this.address.equals(that.address)) {
      return false;
    }
    if (!this.getIdentifier().equals(that.getIdentifier())) {
      return false;
    }
    return this.getMemVec().equals(that.getMemVec());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getMemVec(), getIdentifier(), getAddress(), getPort());
  }

  public boolean isNotEmpty() {
    return !this.equals(EMPTY_NODE);
  }

  public boolean isEmpty() {
    return this.equals(EMPTY_NODE);
  }

  @Override
  public String toString() {
    return "identifier: " + identifier.toString() + "\tmembership vector: " + membershipVector.toString() + "\taddress: " + address + "\tport: "
        + port;
  }
}
