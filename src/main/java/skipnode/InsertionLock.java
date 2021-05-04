package skipnode;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.Semaphore;

public class InsertionLock {

    // Represents an acquired lock from a neighbor.
    public static class NeighborInstance {
        public final SkipNodeIdentity node;
        public final int minLevel;

        public NeighborInstance(SkipNodeIdentity node, int minLevel) {
            this.node = node;
            this.minLevel = minLevel;
        }
    }

    private final Semaphore locked = new Semaphore(1, true);
    public SkipNodeIdentity holder = null;
    public SkipNodeIdentity owner;

    private static final Logger logger = LogManager.getLogger(InsertionLock.class);

    public InsertionLock(SkipNodeIdentity owner) {
        this.owner = owner;
    }
    public boolean startInsertion() {
        logger.debug("starting insertion " +
                "owner_num_id: " + this.owner.getNumID());
        boolean acquired = locked.tryAcquire();
        if(acquired) holder = null;
        return acquired;
    }

    public void endInsertion() {
        logger.debug("ending insertion " +
                "owner_num_id: " + this.owner.getNumID());
        if(holder == null) locked.release();
    }

    public boolean tryAcquire(SkipNodeIdentity receiver) {
        boolean acquired = (receiver.equals(holder)) || locked.tryAcquire();
        if(acquired) holder = receiver;
        return acquired;
    }

    public boolean isLocked() {
        return locked.availablePermits() == 0;
    }

    public boolean isLockedBy(String address, int port) {
        return isLocked() && holder != null && holder.getAddress().equals(address) && holder.getPort() == port;
    }

    public boolean unlockOwned(SkipNodeIdentity owner) {
        if(!this.holder.equals(owner)) return false;
        this.holder = null;
        locked.release();
        return true;
    }
}
