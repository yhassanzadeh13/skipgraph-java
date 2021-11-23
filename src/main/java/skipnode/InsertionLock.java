package skipnode;

import java.util.concurrent.Semaphore;

import log.Log4jLogger;
import org.apache.logging.log4j.LogManager;

/**
 * Represents a lock used that is acquired by neighbouring nodes in order to insert that nodes into
 * look up tables.
 */
public class InsertionLock {

  // Represents an acquired lock from a neighbor.

  private static final Log4jLogger logger =
      new Log4jLogger(LogManager.getLogger(InsertionLock.class));
  private final Semaphore locked = new Semaphore(1, true);
  /**
   * Represents the holder node of this insertion lock.
   */
  public SkipNodeIdentity holder = null;
  /**
   * Represents the node that owns this insertion lock.
   */
  public SkipNodeIdentity owner;

  public InsertionLock(SkipNodeIdentity owner) {
    this.owner = owner;
  }

  /**
   * Starts insertion by acquiring the lock if its available.
   *
   * @return {@code true} if lock is acquired {@code false} otherwise
   */
  public boolean startInsertion() {
    logger.debug().addInt("owner_num_id", this.owner.getIdentifier()).addMsg("starting insertion");
    boolean acquired = locked.tryAcquire();
    if (acquired) {
      holder = null;
    }
    return acquired;
  }

  /**
   * releases the lock if no node holds this insertion lock.
   */
  public void endInsertion() {
    logger.debug().addInt("owner_num_id", this.owner.getIdentifier()).addMsg("ending insertion");
    if (holder == null) {
      locked.release();
    }
  }

  /**
   * Method for trying to acquire insertion lock.
   *
   * @param receiver node that wants to acquire owners insertion lock
   * @return {@code true} if the lock is receiver holds the lock {@code false} otherwise
   */
  public boolean tryAcquire(SkipNodeIdentity receiver) {
    boolean acquired = (receiver.equals(holder)) || locked.tryAcquire();
    if (acquired) {
      holder = receiver;
    }
    return acquired;
  }

  /**
   * Method for checking if the lock is locked.
   *
   * @return {@code true} representing if the lock is locked {@code false} otherwise
   */
  public boolean isLocked() {
    return locked.availablePermits() == 0;
  }

  /**
   * Method for checking if the lock holder of this lock has specified address and port.
   *
   * @param address represents a node address
   * @param port    represents a node port
   * @return {@code true} if the lock holder of this lock has specified address and port {@code
   * false} otherwise
   */
  public boolean isLockedBy(String address, int port) {
    return isLocked()
        && holder != null
        && holder.getAddress().equals(address)
        && holder.getPort() == port;
  }

  /**
   * Releases the lock if the holder parameter is the current holder of the lock.
   *
   * @param holder represents a node
   * @return {@code true} if the lock is held by holder {@code false} otherwise
   */
  public boolean unlockOwned(SkipNodeIdentity holder) {
    if (!this.holder.equals(holder)) {
      return false;
    }
    this.holder = null;
    locked.release();
    return true;
  }

  /**
   * neighbour instance.
   */
  public static class NeighborInstance {

    public final SkipNodeIdentity node;
    public final int minLevel;

    public NeighborInstance(SkipNodeIdentity node, int minLevel) {
      this.node = node;
      this.minLevel = minLevel;
    }
  }
}
