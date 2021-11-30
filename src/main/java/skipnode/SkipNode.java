package skipnode;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.stream.Collectors;

import lookup.LookupTable;
import model.Address;
import network.Network;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import skipnode.InsertionLock.NeighborInstance;

/**
 * Skip Node class.
 */
public class SkipNode implements SkipNodeInterface {

  // The identity to be returned in case the node is currently unreachable (i.e., being inserted.)
  private static final SkipNodeIdentity unavailableIdentity = LookupTable.EMPTY_NODE;
  private static final Logger logger = LogManager.getLogger(SkipNode.class);
  /**
   * Attributes.
   */
  private final Address address;
  private final int numId;
  private final String nameId;
  private final LookupTable lookupTable;
  private final InsertionLock insertionLock;
  private final LinkedBlockingDeque<InsertionLock.NeighborInstance> ownedLocks =
      new LinkedBlockingDeque<>();
  private Network network;
  private boolean inserted = false;
  // Incremented after each lookup table update.
  private int version = 0;

  /**
   * Constructor for SkipNode.
   *
   * @param snId        Skip node identity instance.
   * @param lookupTable Lookup table instance.
   */
  public SkipNode(SkipNodeIdentity snId, LookupTable lookupTable) {
    this.address = snId.getAddress();
    this.numId = snId.getNumId();
    this.nameId = snId.getNameId();
    this.lookupTable = lookupTable;
    this.insertionLock = new InsertionLock(snId);
    insertionLock.startInsertion();
  }

  public int getNumId() {
    return numId;
  }

  public String getNameId() {
    return nameId;
  }

  public LookupTable getLookupTable() {
    return lookupTable;
  }

  public SkipNodeIdentity getIdentity() {
    return new SkipNodeIdentity(nameId, numId, address, version);
  }

  @Override
  public void setMiddleLayer(Network network) {
    this.network = network;
  }

  /**
   * Inserts this SkipNode to the skip graph of the introducer.
   *
   * @param introducerAddress the address of the introducer.
   */
  @Override
  public void insert(Address introducerAddress) {
    // Do not reinsert an already inserted node.
    if (inserted) {
      return;
    }
    // Trivially insert the first node of the skip graph.
    if (introducerAddress == null) {
      logger.debug("num_id: " + getNumId() + " was inserted");
      inserted = true;
      insertionLock.endInsertion();
      return;
    }
    // Try to acquire the locks from all of my neighbors.
    while (true) {
      SkipNodeIdentity left = null;
      SkipNodeIdentity right = null;
      logger.debug("num_id: " + getNumId() + " is searching for its 0-level neighbors");
      // First, find my 0-level neighbor by making a num-id search through the introducer.
      SkipNodeIdentity searchResult = network.searchByNumId(introducerAddress, numId);
      // Get my 0-level left and right neighbors.
      if (getNumId() < searchResult.getNumId()) {
        right = searchResult;
        left = network.getLeftNode(right.getAddress(), right.getNumId(), 0);
      } else {
        left = searchResult;
        right = network.getRightNode(left.getAddress(), left.getNumId(), 0);
      }
      logger.debug(
          "num_id: "
              + getNumId()
              + " has found its 0-level neighbors: "
              + " neighbor_left_num_id: "
              + left.getNumId()
              + " neighbor_right_num_id: "
              + right.getNumId());
      if (acquireNeighborLocks(left, right)) {
        break;
      }
      // When we fail, backoff for a random interval before trying again.
      logger.debug("num_id: " + getNumId() + " could not acquire the locks, backing off");
      int sleepTime = (int) (Math.random() * 2000);
      try {
        Thread.sleep(sleepTime);
      } catch (InterruptedException e) {
        logger.fatal("could not backoff, " + "sleepTime: " + sleepTime, e);
      }
    }
    logger.debug(
        "num_id: "
            + getNumId()
            + " has acquired all the locks: "
            + ownedLocks.stream()
            .map(n -> String.valueOf(n.node.getNumId()))
            .collect(Collectors.joining(", ")));

    // At this point, we should have acquired all of our neighbors. Now, it is time to add them.
    for (InsertionLock.NeighborInstance n : ownedLocks) {
      // Insert the neighbor into my own table.
      insertIntoTable(n.node, n.minLevel);
      // Let the neighbor insert me in its table.
      network.announceNeighbor(n.node.getAddress(), n.node.getNumId(), getIdentity(), n.minLevel);
    }
    // Now, we release all of the locks.
    List<InsertionLock.NeighborInstance> toRelease = new ArrayList<>();
    ownedLocks.drainTo(toRelease);
    // Release the locks.
    toRelease.forEach(n -> {network.unlock(n.node.getAddress(), n.node.getNumId(), getIdentity());});
    // Complete the insertion.
    inserted = true;
    logger.debug("num_id: " + getNumId() + " was inserted");
    insertionLock.endInsertion();
  }

  /**
   * Insert a data node that corresponds to this node. This delegates the work to the middle layer.
   * The node does not know anything about its child nodes after inserting them.
   *
   * @param node Skip node instance.
   */
  public void insertDataNode(SkipNodeInterface node) {
    network.insertDataNode(node);
  }

  /**
   * ... If not all the locks are acquired, the acquired locks are released.
   *
   * @param left  0th level left neighbor.
   * @param right 0th level right neighbor.
   * @return true iff all the locks were acquired.
   */
  public boolean acquireNeighborLocks(SkipNodeIdentity left, SkipNodeIdentity right) {
    // Try to acquire the locks for the left and right neighbors at all the levels.
    SkipNodeIdentity leftNeighbor = left;
    SkipNodeIdentity rightNeighbor = right;
    // This flag will be set to false when we cannot acquire a lock.
    boolean allAcquired = true;
    // These flags will be used to detect when a neighbor
    // at an upper level is the same as the lower one.
    boolean newLeftNeighbor = true;
    boolean newRightNeighbor = true;
    // Climb up the levels and acquire the left and right neighbor locks.
    for (int level = 0; level < lookupTable.getNumLevels(); level++) {
      if (leftNeighbor.equals(LookupTable.EMPTY_NODE)
          && rightNeighbor.equals(LookupTable.EMPTY_NODE)) {
        break;
      }
      if (newLeftNeighbor && !leftNeighbor.equals(LookupTable.EMPTY_NODE)) {
        // Try to acquire the lock for the left neighbor.
        logger.debug(
            "num_id: "
                + getNumId()
                + " is trying to acquire a lock from "
                + "neighbor_left_num_id "
                + leftNeighbor.getNumId());
        boolean acquired =
            network.tryAcquire(
                leftNeighbor.getAddress(),
                leftNeighbor.getNumId(),
                getIdentity(),
                leftNeighbor.version);
        if (!acquired) {
          allAcquired = false;
          break;
        }
        // Add the new lock to our list of locks.
        ownedLocks.add(new InsertionLock.NeighborInstance(leftNeighbor, level));
      }
      if (newRightNeighbor && !rightNeighbor.equals(LookupTable.EMPTY_NODE)) {
        logger.debug(
            "num_id: "
                + getNumId()
                + " is trying to acquire a lock from "
                + "neighbor_right_num_id "
                + rightNeighbor.getNumId());
        // Try to acquire the lock for the right neighbor.
        boolean acquired =
            network.tryAcquire(
                rightNeighbor.getAddress(),
                rightNeighbor.getNumId(),
                getIdentity(),
                rightNeighbor.version);
        if (!acquired) {
          allAcquired = false;
          break;
        }
        // Add the new lock to our list of locks.
        ownedLocks.add(new InsertionLock.NeighborInstance(rightNeighbor, level));
      }
      logger.debug("num_id: " + getNumId() + " is climbing up");
      // Acquire the ladders (i.e., the neighbors at the upper level) and
      // check if they are new neighbors or not. If they are not,
      // we won't need to request a lock from them.
      logger.debug(
          "num_id: "
              + getNumId()
              + " is sending findLadder request to "
              + "neighbor_left_num_id "
              + leftNeighbor.getNumId());
      SkipNodeIdentity leftLadder =
          (leftNeighbor.equals(LookupTable.EMPTY_NODE))
              ? LookupTable.EMPTY_NODE
              : network.findLadder(
              leftNeighbor.getAddress(),
              leftNeighbor.getNumId(),
              level,
              0,
              getNameId());
      newLeftNeighbor = !leftLadder.equals(leftNeighbor);
      logger.debug(
          "num_id: "
              + getNumId()
              + " is sending findLadder request to "
              + "neighbor_right_num_id "
              + rightNeighbor.getNumId());
      SkipNodeIdentity rightLadder =
          (rightNeighbor.equals(LookupTable.EMPTY_NODE))
              ? LookupTable.EMPTY_NODE
              : network.findLadder(
              rightNeighbor.getAddress(),
              rightNeighbor.getNumId(),
              level,
              1,
              getNameId());
      newRightNeighbor = !rightLadder.equals(rightNeighbor);
      leftNeighbor = leftLadder;
      rightNeighbor = rightLadder;
      // It may be the case that we cannot possibly acquire a new neighbor
      // because another concurrent insertion is locking a potential neighbor.
      // This means we should simply fail and let the insertion procedure backoff.
      if (leftLadder.equals(LookupTable.INVALID_NODE)
          || rightLadder.equals(LookupTable.INVALID_NODE)) {
        allAcquired = false;
        break;
      }
      logger.debug("num_id: " + getNumId() + " has climbed up");
    }
    logger.debug("num_id: " + getNumId() + " has completed proposal phase");
    // If we were not able to acquire all the locks, then release the locks that were acquired.
    if (!allAcquired) {
      List<NeighborInstance> toRelease = new ArrayList<>();
      ownedLocks.drainTo(toRelease);
      // Release the locks.
      toRelease.forEach(n -> {network.unlock(n.node.getAddress(), n.node.getNumId(), getIdentity());});
    }
    return allAcquired;
  }

  @Override
  public boolean tryAcquire(SkipNodeIdentity requester, int version) {
    // Naively try to acquire the lock.
    if (!insertionLock.tryAcquire(requester)) {
      logger.debug(
          "num_id: "
              + getNumId()
              + " did not hand over the lock to "
              + requester.getNumId()
              + " because it is already given to "
              + ((insertionLock.holder == null)
              ? this.getNumId()
              : insertionLock.holder.getNumId()));
      return false;
    }
    // After acquiring the lock, make sure that the versions match.
    if (version != this.version) {
      // Otherwise, immediately release and return false.
      insertionLock.unlockOwned(requester);
      return false;
    }
    logger.debug(
        "num_id: "
            + getNumId()
            + " is being locked by "
            + requester.getNumId()
            + " with provided version "
            + version);
    return true;
  }

  @Override
  public boolean unlock(SkipNodeIdentity owner) {
    boolean unlocked = insertionLock.unlockOwned(owner);
    logger.debug(
        "num_id: "
            + getNumId()
            + " has released the lock from "
            + owner.getNumId()
            + ": "
            + unlocked);
    return unlocked;
  }

  /**
   * Returns whether the node is available to be used as a router. If the node is still being
   * inserted, or is a neighbor of a node that is currently being inserted, this will return false.
   *
   * @return whether the node is available for routing or not.
   */
  @Override
  public boolean isAvailable() {
    return inserted && !insertionLock.isLocked();
  }

  /**
   * Finds the `ladder`, i.e. the node that should be used to propagate a newly joined node to the
   * upper layer. Only used by the insertion protocol, and not by the name ID search protocol even
   * though both of them makes use of ladders.
   *
   * @return the `ladder` node information.
   */
  public SkipNodeIdentity findLadder(int level, int direction, String target) {
    logger.debug(
        "num_id: "
            + getNumId()
            + " has received a findLadder request with"
            + " level: "
            + level
            + " direction: "
            + direction
            + " target: "
            + target);
    if (level >= lookupTable.getNumLevels() || level < 0) {
      logger.debug("num_id: " + getNumId() + " is returning a findLadder response");
      return LookupTable.EMPTY_NODE;
    }
    // If the current node and the inserted node have common bits more than the current level,
    // then this node is the neighbor so we return it
    if (SkipNodeIdentity.commonBits(target, getNameId()) > level) {
      logger.debug("num_id: " + getNumId() + " is returning a findLadder response");
      return getIdentity();
    }
    SkipNodeIdentity curr = (direction == 0) ? getLeftNode(level) : getRightNode(level);
    while (!curr.equals(LookupTable.EMPTY_NODE)
        && SkipNodeIdentity.commonBits(target, curr.getNameId()) <= level) {
      logger.debug(
          "num_id: "
              + getNumId()
              + " is in findLadder loop at level "
              + level
              + " with "
              + curr.getNumId());
      // Try to find a new neighbor, but immediately return if the neighbor is locked.
      curr =
          (direction == 0)
              ? network.getLeftNode(false, curr.getAddress(), curr.getNumId(), level)
              : network.getRightNode(curr.getAddress(), curr.getNumId(), level, false);
      // If the potential neighbor is locked, we will get an invalid identity.
      // We should directly return it in that case.
      if (curr.equals(LookupTable.INVALID_NODE)) {
        return curr;
      }
    }
    logger.debug("num_id: " + getNumId() + " is returning a findLadder response");
    return curr;
  }

  /**
   * Given a new neighbor, inserts it to the appropriate levels according to the name ID of the new
   * node.
   *
   * @param newNeighbor the identity of the new neighbor.
   */
  @Override
  public void announceNeighbor(SkipNodeIdentity newNeighbor, int minLevel) {
    insertIntoTable(newNeighbor, minLevel);
  }

  /**
   * Puts the given node into every appropriate level & direction according to its name ID and
   * numerical ID.
   *
   * @param node the node to insert.
   */
  private void insertIntoTable(SkipNodeIdentity node, int minLevel) {
    logger.debug("num_id: " + getNumId() + " has updated its table");
    version++;
    int direction = (node.getNumId() < getNumId()) ? 0 : 1;
    int maxLevel = SkipNodeIdentity.commonBits(getNameId(), node.getNameId());
    for (int i = minLevel; i <= maxLevel; i++) {
      if (direction == 0) {
        updateLeftNode(node, i);
      } else {
        updateRightNode(node, i);
      }
    }
  }

  @Override
  public boolean delete() {
    // TODO Implement
    return false;
  }

  /**
   * Search for the given numID.
   *
   * @param numId The numID to search for
   * @return The SkipNodeIdentity of the node with the given numID. If it does not exist, returns
   *         the SkipNodeIdentity of the SkipNode with NumID closest to the given numID from the
   *         direction the search is initiated. For example: Initiating a search for a SkipNode with
   *         NumID 50 from a SnipNode with NumID 10 will return the SkipNodeIdentity of the SnipNode
   *         with NumID 50 is it exists. If no such SnipNode exists, the SkipNodeIdentity of the
   *         SnipNode whose NumID is closest to 50 among the nodes whose NumID is less than 50 is
   *         returned.
   */
  @Override
  public SkipNodeIdentity searchByNumId(int numId) {
    // If this is the node the search request is looking for, return its identity
    if (numId == this.numId) {
      return getIdentity();
    }
    // Initialize the level to begin looking at
    int level = lookupTable.getNumLevels();
    // If the target is greater than this node's numID, the search should continue to the right
    if (this.numId < numId) {
      // Start from the top, while there is no right neighbor,
      // or the right neighbor's num ID is greater than what we are searching for keep going down
      while (level >= 0) {
        if (lookupTable.getRight(level) == LookupTable.EMPTY_NODE
            || lookupTable.getRight(level).getNumId() > numId) {
          level--;
        } else {
          break;
        }
      }
      // If the level is less than zero, then this node is the closest node to the
      // numID being searched for from the right. Return.
      if (level < 0) {
        return getIdentity();
      }
      // Else, delegate the search to that node on the right
      SkipNodeIdentity delegateNode = lookupTable.getRight(level);
      return network.searchByNumId(delegateNode.getAddress(), delegateNode.getNumId(), numId);
    } else {
      // Start from the top, while there is no right neighbor,
      // or the right neighbor's num ID is greater than what we are searching for keep going down
      while (level >= 0) {
        if (lookupTable.getLeft(level) == LookupTable.EMPTY_NODE
            || lookupTable.getLeft(level).getNumId() < numId) {
          level--;
        } else {
          break;
        }
      }
      // If the level is less than zero, then this node is the closest node to the numID
      // being searched for from the right. Return.
      if (level < 0) {
        return getIdentity();
      }
      // Else, delegate the search to that node on the right
      SkipNodeIdentity delegateNode = lookupTable.getLeft(level);
      return network.searchByNumId(delegateNode.getAddress(), delegateNode.getNumId(), numId);
    }
  }

  @Override
  public boolean isLocked() {
    return insertionLock.isLocked();
  }

  @Override
  public boolean isLockedBy(Address address) {
    return insertionLock.isLockedBy(address);
  }

  @Override
  public boolean terminate() {
    this.network.terminate();
    return true;
  }

  /**
   * Performs a name ID lookup over the skip-graph. If the exact name ID is not found, the most
   * similar one is returned.
   *
   * @param nameId the target name ID.
   * @return the node with the name ID most similar to the target name ID.
   */
  @Override
  public SearchResult searchByNameId(String nameId) {
    if (this.nameId.equals(nameId)) {
      return new SearchResult(getIdentity());
    }
    // If the node is not completely inserted yet, return a tentative identity.
    if (!isAvailable()) {
      logger.debug(
          "num_id: " + getNumId() + " not completely inserted yet, returning a tentative identity");
      return new SearchResult(unavailableIdentity);
    }
    // Find the level in which the search should be started from.
    int level = SkipNodeIdentity.commonBits(this.nameId, nameId);
    if (level < 0) {
      return new SearchResult(getIdentity());
    }
    // Initiate the search.
    return network.searchByNameIdRecursive(address, numId, nameId, level);
  }

  /**
   * Implements the recursive search by name ID procedure.
   *
   * @param targetNameId the target name ID.
   * @param level        the current level.
   * @return the SkipNodeIdentity of the closest SkipNode which has the common prefix
   *         length larger than `level`.
   */
  @Override
  public SearchResult searchByNameIdRecursive(String targetNameId, int level) {
    if (nameId.equals(targetNameId)) {
      return new SearchResult(getIdentity());
    }
    // Buffer contains the `most similar node` to return in case we cannot climb up anymore.
    // At first, we try to set this to the non null potential ladder.
    SkipNodeIdentity potentialLeftLadder = getIdentity();
    SkipNodeIdentity potentialRightLadder = getIdentity();
    SkipNodeIdentity buffer =
        (!potentialLeftLadder.equals(LookupTable.EMPTY_NODE))
            ? potentialLeftLadder
            : potentialRightLadder;
    // This loop will execute and we expand our search window until a ladder is found
    // either on the right or the left.
    while (SkipNodeIdentity.commonBits(targetNameId, potentialLeftLadder.getNameId()) <= level
        && SkipNodeIdentity.commonBits(targetNameId, potentialRightLadder.getNameId()) <= level) {
      // Return the potential ladder as the result if it is the result we are looking for.
      if (potentialLeftLadder.getNameId().equals(targetNameId)) {
        return new SearchResult(potentialLeftLadder);
      }
      if (potentialRightLadder.getNameId().equals(targetNameId)) {
        return new SearchResult(potentialRightLadder);
      }
      // Expand the search window on the level.
      if (!potentialLeftLadder.equals(LookupTable.EMPTY_NODE)) {
        buffer = potentialLeftLadder;
        potentialLeftLadder =
            network.findLadder(
                potentialLeftLadder.getAddress(),
                potentialLeftLadder.getNumId(),
                level,
                0,
                targetNameId);
      }
      if (!potentialRightLadder.equals(LookupTable.EMPTY_NODE)) {
        buffer = potentialRightLadder;
        potentialRightLadder =
            network.findLadder(
                potentialRightLadder.getAddress(),
                potentialRightLadder.getNumId(),
                level,
                1,
                targetNameId);
      }
      // Try to climb up on the either ladder.
      if (SkipNodeIdentity.commonBits(targetNameId, potentialRightLadder.getNameId()) > level) {
        level = SkipNodeIdentity.commonBits(targetNameId, potentialRightLadder.getNameId());
        return network.searchByNameIdRecursive(
            potentialRightLadder.getAddress(),
            potentialRightLadder.getNumId(),
            targetNameId,
            level);
      } else if (SkipNodeIdentity.commonBits(targetNameId, potentialLeftLadder.getNameId())
          > level) {
        level = SkipNodeIdentity.commonBits(targetNameId, potentialLeftLadder.getNameId());
        return network.searchByNameIdRecursive(
            potentialLeftLadder.getAddress(),
            potentialLeftLadder.getNumId(),
            targetNameId,
            level);
      }
      // If we have expanded more than the length of the level,
      // then return the most similar node (buffer).
      if (potentialLeftLadder.equals(LookupTable.EMPTY_NODE)
          && potentialRightLadder.equals(LookupTable.EMPTY_NODE)) {
        return new SearchResult(buffer);
      }
    }
    return new SearchResult(buffer);
  }

  @Override
  public SkipNodeIdentity updateLeftNode(SkipNodeIdentity snId, int level) {
    logger.debug(
        "num_id: "
            + getNumId()
            + " has received a updateLeftNode request with "
            + "skip_node_identity: "
            + snId
            + " level: "
            + level);
    return lookupTable.updateLeft(snId, level);
  }

  @Override
  public SkipNodeIdentity updateRightNode(SkipNodeIdentity snId, int level) {
    logger.debug(
        "num_id: "
            + getNumId()
            + " has received a updateRightNode request with "
            + "skip_node_identity: "
            + snId
            + " level: "
            + level);
    return lookupTable.updateRight(snId, level);
  }

  @Override
  public SkipNodeIdentity getRightNode(int level) {
    logger.debug(
        "num_id: " + getNumId() + " has received a getRightNode request with " + "level: " + level);
    SkipNodeIdentity right = lookupTable.getRight(level);
    SkipNodeIdentity r =
        (right.equals(LookupTable.EMPTY_NODE))
            ? right
            : network.getIdentity(right.getAddress(), right.getNumId());
    logger.debug("num_id: " + getNumId() + " is returning a getRightNode response");
    return r;
  }

  @Override
  public SkipNodeIdentity getLeftNode(int level) {
    logger.debug(
        "num_id: " + getNumId() + " has received a getLeftNode request with " + "level: " + level);
    SkipNodeIdentity left = lookupTable.getLeft(level);
    SkipNodeIdentity r =
        (left.equals(LookupTable.EMPTY_NODE))
            ? left
            : network.getIdentity(left.getAddress(), left.getNumId());
    logger.debug("num_id: " + getNumId() + " is returning a getLeftNode response");
    return r;
  }
}
