package lookup;

import java.util.ArrayList;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import log.Log4jLogger;
import org.apache.logging.log4j.LogManager;
import skipnode.SkipNodeIdentity;

/**
 * ConcurrentLookupTable is a lookup table that supports concurrent calls.
 */
public class ConcurrentLookupTable implements LookupTable {


  // TODO: logger should be passed as a constructor parameter.
  private static final Log4jLogger logger = new Log4jLogger(LogManager.getLogger(ConcurrentLookupTable.class));
  private final SkipNodeIdentity owner;
  private final int numLevels;
  private final ReadWriteLock lock;
  /**
   * All the neighbors are placed in an arraylist, with EMPTY_NODE for empty nodes. The formula to
   * get the index of a neighbor is 2*level for a node on the left side and 2*level+1 for a node on
   * the right side. This is reflected in the getIndex method.
   */
  private final ArrayList<SkipNodeIdentity> nodes;

  /**
   * Constructor for ConcurrentLookupTable.
   *
   * @param numLevels Integer representing number of levels.
   */
  public ConcurrentLookupTable(int numLevels, SkipNodeIdentity owner) {
    this.owner = owner;
    this.numLevels = numLevels;
    lock = new ReentrantReadWriteLock(true);
    nodes = new ArrayList<>(2 * numLevels);
    for (int i = 0; i < 2 * numLevels; i++) {
      nodes.add(i, LookupTable.EMPTY_NODE);
    }
  }

  @Override
  public SkipNodeIdentity updateLeft(SkipNodeIdentity node, int level) {
    lock.writeLock().lock();

    int idx = getIndex(Direction.LEFT, level);
    if (idx >= nodes.size()) {
      lock.writeLock().unlock();
      return LookupTable.EMPTY_NODE;
    }
    SkipNodeIdentity prev = nodes.set(idx, node);

    lock.writeLock().unlock();


    //    logger.debug().addInt("owner_num_id", owner.getIdentifier()).addInt("neighbor_num_id", node.getIdentifier()).addInt("level", level).addMsg(
    //        "updated left neighbor in lookup table");
    return prev;
  }

  @Override
  public SkipNodeIdentity updateRight(SkipNodeIdentity node, int level) {
    lock.writeLock().lock();

    int idx = getIndex(Direction.RIGHT, level);
    if (idx >= nodes.size()) {
      lock.writeLock().unlock();
      return LookupTable.EMPTY_NODE;
    }
    SkipNodeIdentity prev = nodes.set(idx, node);

    lock.writeLock().unlock();

    //    logger.debug().addInt("owner_num_id", owner.getIdentifier()).addInt("neighbor_num_id", node.getIdentifier()).addInt("level", level).addMsg(
    //    logger
    //        .debug()
    //        .addInt("owner_num_id", owner.getIdentifier())
    //        .addInt("neighbor_num_id", node.getIdentifier())
    //        .addInt("level", level)
    //        .addMsg("updated right in lookup table");
    return prev;
  }

  @Override
  public SkipNodeIdentity getRight(int level) {
    lock.readLock().lock();

    int idx = getIndex(Direction.RIGHT, level);
    SkipNodeIdentity node = (idx < nodes.size()) ? nodes.get(idx) : LookupTable.EMPTY_NODE;

    lock.readLock().unlock();
    return node;
  }

  @Override
  public SkipNodeIdentity getLeft(int level) {
    lock.readLock().lock();

    int idx = getIndex(Direction.LEFT, level);
    SkipNodeIdentity node = (idx < nodes.size()) ? nodes.get(idx) : LookupTable.EMPTY_NODE;

    lock.readLock().unlock();
    return node;
  }

  @Override
  public boolean isLeftNeighbor(SkipNodeIdentity neighbor, int level) {
    lock.readLock().lock();
    boolean exists = getLeft(level).equals(neighbor);
    lock.readLock().unlock();
    return exists;
  }

  @Override
  public boolean isRightNeighbor(SkipNodeIdentity neighbor, int level) {
    lock.readLock().lock();
    boolean exists = getRight(level).equals(neighbor);
    lock.readLock().unlock();
    return exists;
  }

  @Override
  public int getNumLevels() {
    return this.numLevels;
  }

  private int getIndex(Direction dir, int level) {
    if (level < 0) {
      return Integer.MAX_VALUE;
    }
    if (dir == Direction.LEFT) {
      return level * 2;
    } else {
      return level * 2 + 1;
    }
  }
}
