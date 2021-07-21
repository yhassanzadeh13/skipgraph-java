package skipnode;

import log.Log4jLogger;
import org.apache.logging.log4j.LogManager;

import java.io.Serializable;
import java.util.Objects;

// Basic skipnode.SkipNodeIdentity class
public class SkipNodeIdentity implements Serializable, Comparable<SkipNodeIdentity> {

  private final String nameID;
  private final int numID;
  private final String address;
  private final int port;

  // Denotes the lookup table version.
  public int version;

  private static final Log4jLogger logger = new Log4jLogger(
      LogManager.getLogger(SkipNodeIdentity.class));

  public SkipNodeIdentity(String nameID, int numID, String address, int port, int version) {
    this.nameID = nameID;
    this.numID = numID;
    this.address = address;
    this.port = port;
    this.version = version;
  }

  public SkipNodeIdentity(String nameID, int numID, String address, int port) {
    this(nameID, numID, address, port, 0);
  }

  public String getNameID() {
    return nameID;
  }

  public int getNumID() {
    return numID;
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
    return getNumID() == that.getNumID() &&
        getNameID().equals(that.getNameID()) &&
        getAddress().equals(that.getAddress()) &&
        getPort() == that.getPort();
  }

  public static int commonBits(String name1, String name2) {
    logger.debug().
        Str("name_1", name1).
        Str("name_2", name2).
        Msg("calculating common bits");
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

  @Override
  public int hashCode() {
    return Objects.hash(getNameID(), getNumID(), getAddress(), getPort());
  }

  @Override
  public String toString() {
    return "Name ID: " + nameID + "\tNum ID: " + numID + "\tAddress: " + address + "\tPort: "
        + port;
  }

  @Override
  public int compareTo(SkipNodeIdentity o) {
    logger.debug().
        Int("num_id", this.numID).
        Int("compared_num_id", o.getNumID()).
        Msg("currently comparing");
    return Integer.compare(numID, o.numID);
  }
}
