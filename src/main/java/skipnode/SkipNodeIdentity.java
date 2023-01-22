package skipnode;

import static lookup.LookupTable.EMPTY_NODE;

import java.io.Serializable;
import java.util.Objects;

import model.identifier.Identifier;
import model.identifier.MembershipVector;

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
   * @param identifier       identifier of the node (aka numerical ID).
   * @param membershipVector membership vector of the node (aka name ID).
   * @param address          String representing the address of the node.
   * @param port             Integer representing the port of the node.
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
  @Deprecated
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
    SkipNodeIdentity that = (SkipNodeIdentity) o;
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
