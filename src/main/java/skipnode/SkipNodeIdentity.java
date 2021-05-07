package skipnode;

import misc.JsonMessage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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

    private static final Logger logger = LogManager.getLogger(SkipNodeIdentity.class);

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

    public int getPort() {return port;}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SkipNodeIdentity that = (SkipNodeIdentity) o;
        return getNumID() == that.getNumID() &&
                getNameID().equals(that.getNameID()) &&
                getAddress().equals(that.getAddress()) &&
                getPort() == that.getPort();
    }

    public static int commonBits(String name1, String name2) {
        logger.debug(new JsonMessage().
                add("name_1", name1).
                add("name_2", name2).
                add("msg", "calculating common bits").
                toObjectMessage());
        if(name1 == null || name2 == null) {
            return -1;
        }
        if(name1.length() != name2.length())
            return -1;
        int i = 0;
        while(i < name1.length() && name1.charAt(i) == name2.charAt(i)) i++;
        return i;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getNameID(), getNumID(), getAddress(), getPort());
    }

    @Override
    public String toString() {
        return "Name ID: "+nameID+"\tNum ID: "+numID+"\tAddress: "+address+"\tPort: "+port;
    }

    @Override
    public int compareTo(SkipNodeIdentity o) {
        logger.debug(new JsonMessage().
                add("num_id", this.numID).
                add("skip_node_identity", o).
                add("msg", "currently comparing").
                toObjectMessage());
        return Integer.compare(numID, o.numID);
    }
}
