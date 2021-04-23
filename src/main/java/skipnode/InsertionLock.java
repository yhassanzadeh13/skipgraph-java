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
    public SkipNodeIdentity owner = null;

    private static final Logger logger = LogManager.getLogger(InsertionLock.class);

    public boolean startInsertion() {
        logger.debug("starting insertion");
        boolean acquired = locked.tryAcquire();
        if(acquired) owner = null;
        return acquired;
    }

    public void endInsertion() {
        logger.debug("ending insertion");
        if(owner == null) locked.release();
    }

    public boolean tryAcquire(SkipNodeIdentity receiver) {
        boolean acquired = (receiver.equals(owner)) || locked.tryAcquire();
        if(acquired) owner = receiver;
        return acquired;
    }

    public boolean isLocked() {
        return locked.availablePermits() == 0;
    }

    public boolean isLockedBy(String address, int port) {
        return isLocked() && owner != null && owner.getAddress().equals(address) && owner.getPort() == port;
    }

    public boolean unlockOwned(SkipNodeIdentity owner) {
        if(!this.owner.equals(owner)) return false;
        this.owner = null;
        locked.release();
        return true;
    }
}
