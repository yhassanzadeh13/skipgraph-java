package skipnode;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingDeque;

public class LockManager {
    private LinkedBlockingDeque<InsertionLock.NeighborInstance> ownedLocks;

    public LockManager() {
        this.ownedLocks = new LinkedBlockingDeque<>();
    }

    public void addLock(SkipNodeIdentity identity, int level){
        this.ownedLocks.add(new InsertionLock.NeighborInstance(identity, level));
    }

    public List<InsertionLock.NeighborInstance> releaseAll(){
        List<InsertionLock.NeighborInstance> toRelease = new ArrayList<>();
        ownedLocks.drainTo(toRelease);
        return toRelease;
    }

    @Override
    public String toString() {
        String str = "";
        for(InsertionLock.NeighborInstance n: ownedLocks){
            str = " " + n.node.getNumID();
        }
        return str;
    }
}
