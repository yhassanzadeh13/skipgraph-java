package skipnode;

import misc.JsonMessage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.Semaphore;

/**
 * Represents a lock used that is acquired by neighbouring nodes in order to insert that nodes
 * into look up tables
 */
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

    /**
     * Represents the holder node of this insertion lock
     */
    public SkipNodeIdentity holder = null;
    /**
     * Represents the node that owns this insertion lock
     */
    public SkipNodeIdentity owner;

    private final Semaphore locked = new Semaphore(1, true);
    private static final Logger logger = LogManager.getLogger(InsertionLock.class);

    public InsertionLock(SkipNodeIdentity owner) {
        this.owner = owner;
    }

    /**
     * Starts insertion by acquiring the lock if its available
     * @return {@code true} if lock is acquired {@code false} otherwise
     */
    public boolean startInsertion() {
        logger.debug(new JsonMessage().
                add("owner_num_id", this.owner.getNumID()).
                add("msg", "starting insertion").
                toObjectMessage());
        boolean acquired = locked.tryAcquire();
        if(acquired) holder = null;
        return acquired;
    }

    /**
     * releases the lock if no node holds this insertion lock
     */
    public void endInsertion() {
        logger.debug(new JsonMessage().
                add("owner_num_id", this.owner.getNumID()).
                add("msg", "ending insertion").
                toObjectMessage());
        if(holder == null) locked.release();
    }

    /**
     *
     * @param receiver node that wants to acquire owners insertion lock
     * @return {@code true} if the lock is receiver holds the lock {@code false} otherwise
     */
    public boolean tryAcquire(SkipNodeIdentity receiver) {
        boolean acquired = (receiver.equals(holder)) || locked.tryAcquire();
        if(acquired) holder = receiver;
        return acquired;
    }

    /**
     *
     * @return {@code true} representing if the lock is locked {@code false} otherwise
     */
    public boolean isLocked() {
        return locked.availablePermits() == 0;
    }

    /**
     *
     * @param address represents a node address
     * @param port represents a node port
     * @return {@code true} if the lock holder of this lock has specified address and port {@code false} otherwise
     */
    public boolean isLockedBy(String address, int port) {
        return isLocked() && holder != null && holder.getAddress().equals(address) && holder.getPort() == port;
    }

    /**
     * Releases the lock if the holder parameter is the current holder of the lock
     * @param holder represents a node
     * @return {@code true} if the lock is held by holder {@code false} otherwise
     */
    public boolean unlockOwned(SkipNodeIdentity holder) {
        if(!this.holder.equals(holder)) return false;
        this.holder = null;
        locked.release();
        return true;
    }
}
