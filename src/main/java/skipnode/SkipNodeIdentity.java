package skipnode;

import java.io.Serializable;
import java.util.Objects;

import log.Log4jLogger;
import model.Address;
import org.apache.logging.log4j.LogManager;

/**
 * Basic skipnode.SkipNodeIdentity class
 */
public class SkipNodeIdentity implements Serializable, Comparable<SkipNodeIdentity> {

  private static final Log4jLogger logger =
      new Log4jLogger(LogManager.getLogger(SkipNodeIdentity.class));
  private final String nameId;
  private final int numId;
  private final Address address;
  // Denotes the lookup table version.
  public int version;

  /**
   * Constructor for SkipNodeIdentity.
   *
   * @param nameId  name id of the node.
   * @param numId   numerical id of the node.
   * @param address representing the address of the node.
   * @param version Integer representing the version.
   */
  public SkipNodeIdentity(String nameId, int numId, Address address, int version) {
    this.nameId = nameId;
    this.numId = numId;
    this.address = address;
    this.version = version;
  }

  public SkipNodeIdentity(String nameId, int numId, Address address) {
    this(nameId, numId, address, 0);
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

  public String getNameId() {
    return nameId;
  }

  public int getNumId() {
    return numId;
  }

  public Address getAddress() {
    return address;
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
    return getNumId() == that.getNumId()
        && getNameId().equals(that.getNameId())
        && getAddress().equals(that.getAddress());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getNameId(), getNumId(), getAddress());
  }

  @Override
  public String toString() {
    return "Name ID: "
        + nameId
        + "\tNum ID: "
        + numId
        + "\tAddress: "
        + address;
  }

  @Override
  public int compareTo(SkipNodeIdentity o) {
    logger
        .debug()
        .addInt("num_id", this.numId)
        .addInt("compared_num_id", o.getNumId())
        .addMsg("currently comparing");
    return Integer.compare(numId, o.numId);
  }
}
