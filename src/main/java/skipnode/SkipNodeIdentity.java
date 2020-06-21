package skipnode;

import java.util.Objects;

// Basic skipnode.SkipNodeIdentity class
public class SkipNodeIdentity {
    private final String nameID;
    private final int numID;
    public SkipNodeIdentity(String nameID, int numID){
        this.nameID=nameID;
        this.numID=numID;
    }

    public String getNameID() {
        return nameID;
    }

    public int getNumID() {
        return numID;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SkipNodeIdentity that = (SkipNodeIdentity) o;
        return getNumID() == that.getNumID() &&
                Objects.equals(getNameID(), that.getNameID());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getNameID(), getNumID());
    }
}
