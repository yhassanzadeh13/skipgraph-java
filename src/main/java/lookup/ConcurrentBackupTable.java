package lookup;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;
import skipnode.SkipNodeIdentity;


/**
 * ConcurrentLookupTable is a backup table that supports concurrent calls.
 */
public class ConcurrentBackupTable implements LookupTable {

  private final int numLevels;
  private final int maxSize;
  private final ReadWriteLock lock;
  /**
   * All the neighbors are placed in an arraylist, with EMPTY_NODE for empty nodes. The formula to
   * get the index of a neighbor is 2*level for a node on the left side and 2*level+1 for a node on
   * the right side. This is reflected in the getIndex method.
   */
  private final ArrayList<List<SkipNodeIdentity>> nodes;
  private final List<SkipNodeIdentity> emptyLevel = new ArrayList<>();

  private enum Direction {
    LEFT,
    RIGHT
  }

  /**
   * Constructor for ConcurrentBackupTable.
   *
   * @param numLevels Integer representing number of levels.
   * @param maxSize Integer representing the maximum size.
   */
  public ConcurrentBackupTable(int numLevels, int maxSize) {
    this.numLevels = numLevels;
    this.maxSize = maxSize;
    lock = new ReentrantReadWriteLock(true);
    nodes = new ArrayList<>(2 * numLevels);
    for (int i = 0; i < 2 * numLevels; i++) {
      // At each lookup table entry, we store a list of nodes instead of a single node.
      nodes.add(i, new ArrayList<>(maxSize));
    }
  }

  public ConcurrentBackupTable(int numLevels) {
    this(numLevels, 100);
  }

  /**
   * Method for adding node to the right.
   *
   * @param node SkipNode instance to be added.
   * @param level Integer representing the level.
   * @return list of neighbour nodes.
   */
  public List<SkipNodeIdentity> addRightNode(SkipNodeIdentity node, int level) {
    int trial = 1;
    // Exponential backoff for writing.
    while (!lock.writeLock().tryLock()) {
      try {
        Thread.sleep((int) (Math.random() * Math.pow(2, trial - 1) * 50));
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
      trial++;
    }
    int idx = getIndex(Direction.RIGHT, level);
    List<SkipNodeIdentity> entry = nodes.get(idx);
    entry.add(node);
    // Sort the node list in ascending order.
    Collections.sort(entry);
    // Remove the last node if it exceeds the max node list size.
    if (entry.size() > this.maxSize) {
      entry.remove(entry.size() - 1);
    }
    lock.writeLock().unlock();
    return entry;
  }

  /**
   * Method for adding node to the left.
   *
   * @param node SkipNode instance to be added.
   * @param level Integer representing the level.
   * @return list of neighbour nodes.
   */
  public List<SkipNodeIdentity> addLeftNode(SkipNodeIdentity node, int level) {
    int trial = 1;
    // Exponential backoff for writing.
    while (!lock.writeLock().tryLock()) {
      try {
        Thread.sleep((int) (Math.random() * Math.pow(2, trial) * 50));
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
      trial++;
    }
    int idx = getIndex(Direction.LEFT, level);
    List<SkipNodeIdentity> entry = nodes.get(idx);
    entry.add(node);
    // Sort the node list in a descending order.
    Collections.sort(entry);
    Collections.reverse(entry);
    // Remove the latest node if it exceeds the max node list size.
    if (entry.size() > this.maxSize) {
      entry.remove(entry.size() - 1);
    }
    lock.writeLock().unlock();
    return entry;
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

  @Override
  public SkipNodeIdentity updateLeft(SkipNodeIdentity node, int level) {
    addLeftNode(node, level);
    return getLeft(level);
  }

  @Override
  public SkipNodeIdentity updateRight(SkipNodeIdentity node, int level) {
    addRightNode(node, level);
    return getRight(level);
  }

  @Override
  public List<SkipNodeIdentity> getRights(int level) {
    lock.readLock().lock();
    int idx = getIndex(Direction.RIGHT, level);
    if (idx >= nodes.size()) {
      return emptyLevel;
    }
    // This works because SkipNodeIdentity is immutable.
    ArrayList<SkipNodeIdentity> result = new ArrayList<>(nodes.get(idx));
    lock.readLock().unlock();
    return result;
  }

  @Override
  public List<SkipNodeIdentity> getLefts(int level) {
    lock.readLock().lock();
    int idx = getIndex(Direction.LEFT, level);
    if (idx >= nodes.size()) {
      lock.readLock().unlock();
      return emptyLevel;
    }
    // This works because SkipNodeIdentity is immutable.
    ArrayList<SkipNodeIdentity> result = new ArrayList<>(nodes.get(idx));
    lock.readLock().unlock();
    return result;
  }

  @Override
  public SkipNodeIdentity getRight(int level) {
    lock.readLock().lock();
    int idx = getIndex(Direction.RIGHT, level);
    SkipNodeIdentity node = LookupTable.EMPTY_NODE;
    // If we have a non-empty backup list at the index, return the first
    // element of the backup list.
    if (idx < nodes.size() && nodes.get(idx).size() > 0) {
      node = nodes.get(idx).get(0);
    }
    lock.readLock().unlock();
    return node;
  }

  @Override
  public SkipNodeIdentity getLeft(int level) {
    lock.readLock().lock();
    int idx = getIndex(Direction.LEFT, level);
    SkipNodeIdentity node = LookupTable.EMPTY_NODE;
    // If we have a non-empty backup list at the index, return the first
    // element of the backup list.
    if (idx < nodes.size() && nodes.get(idx).size() > 0) {
      node = nodes.get(idx).get(0);
    }
    lock.readLock().unlock();
    return node;
  }

  @Override
  public boolean isLeftNeighbor(SkipNodeIdentity neighbor, int level) {
    return getLefts(level).stream().anyMatch(x -> x.equals(neighbor));
  }

  @Override
  public boolean isRightNeighbor(SkipNodeIdentity neighbor, int level) {
    return getRights(level).stream().anyMatch(x -> x.equals(neighbor));
  }

  @Override
  public SkipNodeIdentity removeLeft(int level) {
    SkipNodeIdentity lft = getLeft(level);
    removeLeft(lft, level);
    return getLeft(level);
  }

  /**
   * Method for removing a node at the specified level.
   *
   * @param sn node to be removed.
   * @param level The level from which to remove the left neighbor.
   * @return list of all the left neighbors on the given level.
   */
  public List<SkipNodeIdentity> removeLeft(SkipNodeIdentity sn, int level) {
    lock.writeLock().lock();
    List<SkipNodeIdentity> leftNodes = getLefts(level);
    leftNodes.removeIf(nd -> nd.equals(sn));
    lock.writeLock().unlock();
    return leftNodes;
  }

  @Override
  public SkipNodeIdentity removeRight(int level) {
    SkipNodeIdentity right = getRight(level);
    removeRight(right, level);
    return getRight(level);
  }

  /**
   * Method for removing a node at the specified level.
   *
   * @param sn node to be removed.
   * @param level The level from which to remove the right neighbor.
   * @return list of all the right neighbors on the given level.
   */
  public List<SkipNodeIdentity> removeRight(SkipNodeIdentity sn, int level) {
    lock.writeLock().lock();
    List<SkipNodeIdentity> rightNodes = getRights(level);
    rightNodes.removeIf(nd -> nd.equals(sn));
    lock.writeLock().unlock();
    return rightNodes;
  }

  /**
   * Returns the new neighbors (unsorted) of a newly inserted node. It is assumed that the newly
   * inserted node will be a neighbor to the owner of this lookup table.
   *
   * @param owner     the identity of the owner of the lookup table.
   * @param newNameId the name ID of the newly inserted node.
   * @param newNumId  the num ID of the newly inserted node.
   * @param level     the level of the new neighbor.
   * @return the list of neighbors (both right and left) of the newly inserted node.
   */
  @Override
  public TentativeTable acquireNeighbors(SkipNodeIdentity owner, int newNumId, String newNameId,
      int level) {
    lock.readLock().lock();
    // We will return an unsorted list of level-lists.
    List<List<SkipNodeIdentity>> newTable = new ArrayList<>(numLevels);
    for (int i = 0; i < numLevels; i++) {
      newTable.add(new ArrayList<>());
    }
    // Add the neighbors from the 0-level neighbors.
    nodes.stream()
        .limit(2)
        .flatMap(Collection::stream)
        .filter(x -> !x.equals(LookupTable.EMPTY_NODE))
        .forEach(neighbor -> {
          int l = SkipNodeIdentity.commonBits(neighbor.getNameId(), newNameId);
          // Add the neighbor at the max level.
          newTable.get(l).add(neighbor);
        });
    // Add the owner of this lookup table to the appropriate levels.
    int l = SkipNodeIdentity.commonBits(owner.getNameId(), newNameId);
    newTable.get(l).add(owner);
    lock.readLock().unlock();
    // Return the new lookup table.
    return new TentativeTable(true, -1, newTable);
  }

  @Override
  public void initializeTable(SkipNodeIdentity owner, TentativeTable tentativeTable) {
    lock.writeLock().lock();
    // Insert every neighbor at the correct level & direction.
    for (int l = 0; l < tentativeTable.neighbors.size(); l++) {
      List<SkipNodeIdentity> leftList = tentativeTable.neighbors.get(l).stream()
          .filter(x -> x.getNumId() <= owner.getNumId())
          .collect(Collectors.toList());
      List<SkipNodeIdentity> rightList = tentativeTable.neighbors.get(l).stream()
          .filter(x -> x.getNumId() > owner.getNumId())
          .collect(Collectors.toList());
      for (int j = 0; j <= l; j++) {
        int leftIndex = getIndex(Direction.LEFT, j);
        int rightIndex = getIndex(Direction.RIGHT, j);
        nodes.get(leftIndex).addAll(leftList);
        nodes.get(rightIndex).addAll(rightList);
      }
    }
    // Sort all the entries.
    for (int l = 0; l < numLevels; l++) {
      int leftIndex = getIndex(Direction.LEFT, l);
      int rightIndex = getIndex(Direction.RIGHT, l);
      Collections.sort(nodes.get(leftIndex));
      Collections.reverse(nodes.get(leftIndex));
      Collections.sort(nodes.get(rightIndex));
    }
    lock.writeLock().unlock();
  }

  @Override
  public int getNumLevels() {
    return this.numLevels;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder("ConcurrentBackupTable");
    sb.append('\n');
    for (int i = getNumLevels() - 1; i >= 0; i--) {
      sb.append("Level:\t");
      sb.append(i);
      sb.append('\n');
      sb.append("Lefts:\t");
      List<SkipNodeIdentity> lefts = getLefts(i);
      for (int j = lefts.size() - 1; j >= 0; j--) {
        sb.append(lefts.get(j).getNameId());
        sb.append('\t');
      }

      sb.append("Rights:\t");
      List<SkipNodeIdentity> rights = getRights(i);
      for (int j = 0; j < rights.size(); j++) {
        sb.append(rights.get(j).getNameId());
        sb.append('\t');
      }
      sb.append('\n');
    }
    return sb.toString();
  }
}

