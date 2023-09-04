package skipnode;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.stream.Collectors;

import lookup.LookupTable;
import middlelayer.MiddleLayer;
import model.identifier.Identifier;
import model.identifier.MembershipVector;
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

  private final SkipNodeIdentity identity;

  private final LookupTable lookupTable;
  private final InsertionLock insertionLock;
  private final LinkedBlockingDeque<InsertionLock.NeighborInstance> ownedLocks = new LinkedBlockingDeque<>();
  private MiddleLayer middleLayer;
  private boolean inserted = false;
  // Incremented after each lookup table update.
  private int version = 0;

  /**
   * Constructor for SkipNode.
   *
   * @param identity    Skip Graph identity of the node.
   * @param lookupTable Lookup table instance of the node.
   */
  public SkipNode(SkipNodeIdentity identity, LookupTable lookupTable) {
    this.identity = identity;
    this.lookupTable = lookupTable;
    this.insertionLock = new InsertionLock(identity);
    insertionLock.startInsertion();
  }

  public LookupTable getLookupTable() {
    return lookupTable;
  }

  public SkipNodeIdentity getIdentity() {
    return this.identity;
  }

  public Identifier getIdentifier() {
    return this.identity.getIdentifier();
  }

  @Override
  public void setMiddleLayer(MiddleLayer middleLayer) {
    this.middleLayer = middleLayer;
  }

  /**
   * Inserts this SkipNode to the skip graph of the introducer.
   *
   * @param introducerAddress the address of the introducer.
   * @param introducerPort    the port of the introducer.
   */
  @Override
  public void insert(String introducerAddress, int introducerPort) {
    // Do not reinsert an already inserted node.
    if (inserted) {
      return;
    }
    // Trivially insert the first node of the skip graph.
    if (introducerAddress == null) {
      inserted = true;
      insertionLock.endInsertion();
      return;
    }
    // Try to acquire the locks from all of my neighbors.
    while (true) {
      SkipNodeIdentity left;
      SkipNodeIdentity right;

      // First, find my 0-level neighbor by making an identifier search through the introducer.
      SkipNodeIdentity searchResult = middleLayer.searchByIdentifier(this.identity.getIdentifier(), introducerAddress, introducerPort);
      // Get my 0-level left and right neighbors.
      if (this.identity.getIdentifier().comparedTo(searchResult.getIdentifier()) < 0) {
        right = searchResult;
        left = middleLayer.getLeftNeighborOf(right.getAddress(), right.getPort(), right.getIdentifier(), 0);
      } else {
        left = searchResult;
        right = middleLayer.getRightNeighborOf(left.getAddress(), left.getPort(), left.getIdentifier(), 0);
      }
      logger.debug("identifier: " + this.identity.getIdentifier().toString() + " has found its 0-level neighbors: " + " neighbor_left_identifier: "
          + left.getIdentifier() + " neighbor_right_identifier: " + right.getIdentifier());

      if (acquireNeighborLocks(left, right)) {
        break;
      }
      // When we fail, backoff for a random interval before trying again.
      logger.debug("identifier: " + this.getIdentity().getIdentifier().toString() + " could not acquire the locks, backing off");
      int sleepTime = (int) (Math.random() * 2000);
      try {
        Thread.sleep(sleepTime);
      } catch (InterruptedException e) {
        logger.fatal("could not backoff, " + "sleepTime: " + sleepTime, e);
      }
    }
    logger.debug("identifier: " + this.getIdentity().getIdentifier().toString() + " has acquired all the locks: " + ownedLocks.stream()
        .map(n -> String.valueOf(n.node.getIdentifier()))
        .collect(Collectors.joining(", ")));

    // At this point, we should have acquired all of our neighbors. Now, it is time to add them.
    for (InsertionLock.NeighborInstance n : ownedLocks) {
      // Insert the neighbor into my own table.
      insertIntoTable(n.node, n.minLevel);
      // Let the neighbor insert me in its table.
      middleLayer.announceNeighbor(n.node.getAddress(), n.node.getPort(), n.node.getIdentifier(), getIdentity(), n.minLevel);
    }
    // Now, we release all the locks.
    List<InsertionLock.NeighborInstance> toRelease = new ArrayList<>();
    ownedLocks.drainTo(toRelease);
    // Release the locks.
    toRelease.forEach(n -> {
      middleLayer.unlock(n.node.getAddress(), n.node.getPort(), n.node.getIdentifier(), getIdentity());
    });
    // Complete the insertion.
    inserted = true;
    logger.debug("identifier: " + this.identity.getIdentifier() + " was inserted");
    insertionLock.endInsertion();
  }

  /**
   * Insert a data node that corresponds to this node. This delegates the work to the middle layer.
   * The node does not know anything about its child nodes after inserting them.
   *
   * @param node Skip node instance.
   */
  public void insertDataNode(SkipNodeInterface node) {
    middleLayer.insertDataNode(node);
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
      if (leftNeighbor.equals(LookupTable.EMPTY_NODE) && rightNeighbor.equals(LookupTable.EMPTY_NODE)) {
        break;
      }
      if (newLeftNeighbor && !leftNeighbor.equals(LookupTable.EMPTY_NODE)) {
        // Try to acquire the lock for the left neighbor.

        boolean acquired = middleLayer.tryAcquire(leftNeighbor.getAddress(), leftNeighbor.getPort(), leftNeighbor.getIdentifier(), getIdentity());
        if (!acquired) {
          allAcquired = false;
          break;
        }
        // Add the new lock to our list of locks.
        ownedLocks.add(new InsertionLock.NeighborInstance(leftNeighbor, level));
      }
      if (newRightNeighbor && !rightNeighbor.equals(LookupTable.EMPTY_NODE)) {
        logger.debug("identifier: " + this.identity.getIdentifier() + " is trying to acquire a lock from " + "neighbor_right_num_id "
            + rightNeighbor.getIdentifier());
        // Try to acquire the lock for the right neighbor.
        boolean acquired = middleLayer.tryAcquire(rightNeighbor.getAddress(), rightNeighbor.getPort(), rightNeighbor.getIdentifier(), getIdentity());
        if (!acquired) {
          allAcquired = false;
          break;
        }
        // Add the new lock to our list of locks.
        ownedLocks.add(new InsertionLock.NeighborInstance(rightNeighbor, level));
      }

      // Acquire the ladders (i.e., the neighbors at the upper level) and
      // check if they are new neighbors or not. If they are not,
      // we won't need to request a lock from them.
      // TODO: these must be encapsulated as functions.
      SkipNodeIdentity leftLadder = (leftNeighbor.equals(LookupTable.EMPTY_NODE)) ? LookupTable.EMPTY_NODE
                                                                                  : middleLayer.findLadder(leftNeighbor.getAddress(),
                                                                                      leftNeighbor.getPort(), leftNeighbor.getIdentifier(), level, 0,
                                                                                      // TODO: left or right must be defined as constants.
                                                                                      this.identity.getMemVec());
      newLeftNeighbor = !leftLadder.equals(leftNeighbor);

      SkipNodeIdentity rightLadder = (rightNeighbor.equals(LookupTable.EMPTY_NODE)) ? LookupTable.EMPTY_NODE
                                                                                    : middleLayer.findLadder(rightNeighbor.getAddress(),
                                                                                        rightNeighbor.getPort(), rightNeighbor.getIdentifier(), level,
                                                                                        1, this.identity.getMemVec());

      newRightNeighbor = !rightLadder.equals(rightNeighbor);
      leftNeighbor = leftLadder;
      rightNeighbor = rightLadder;
      // It may be the case that we cannot possibly acquire a new neighbor
      // because another concurrent insertion is locking a potential neighbor.
      // This means we should simply fail and let the insertion procedure backoff.
      if (leftLadder.equals(LookupTable.INVALID_NODE) || rightLadder.equals(LookupTable.INVALID_NODE)) {
        allAcquired = false;
        break;
      }
      logger.debug("identifier: " + this.identity.getIdentifier() + " has climbed up");
    }
    logger.debug("identifier: " + this.identity.getIdentifier() + " has completed proposal phase");
    // If we were not able to acquire all the locks, then release the locks that were acquired.
    if (!allAcquired) {
      List<NeighborInstance> toRelease = new ArrayList<>();
      ownedLocks.drainTo(toRelease);
      // Release the locks.
      toRelease.forEach(n -> {
        middleLayer.unlock(n.node.getAddress(), n.node.getPort(), n.node.getIdentifier(), getIdentity());
      });
    }
    return allAcquired;
  }

  @Override
  public boolean tryAcquire(SkipNodeIdentity requester) {
    // Naively try to acquire the lock.
    if (!insertionLock.tryAcquire(requester)) {
      logger.debug(
          "identifier: " + this.identity.getIdentifier() + " did not hand over the lock to " + requester.getIdentifier() + " because it " + "is"
              + " already given to " + ((insertionLock.holder == null) ? this.getIdentity() : insertionLock.holder.getIdentifier()));
      return false;
    }
    // After acquiring the lock, make sure that the versions match.
    if (version != this.version) {
      // Otherwise, immediately release and return false.
      insertionLock.unlockOwned(requester);
      return false;
    }
    logger.debug(
        "identifier: " + this.identity.getIdentifier() + " is being locked by " + requester.getIdentifier() + " with provided version " + version);
    return true;
  }

  @Override
  public boolean unlock(SkipNodeIdentity owner) {
    boolean unlocked = insertionLock.unlockOwned(owner);
    logger.debug("identifier: " + this.identity.getIdentifier() + " has released the lock from " + owner.getIdentifier() + ": " + unlocked);
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
   * upper layer. Only used by the insertion protocol, and not by the membership vector search protocol even
   * though both of them makes use of ladders.
   *
   * @return the `ladder` node information.
   */
  public SkipNodeIdentity findLadder(int level, int direction, MembershipVector target) {
    logger.debug(
        "num_id: " + getIdentity().getIdentifier() + " has received a findLadder request with" + " level: " + level + " direction: " + direction
            + " target: " + target);
    if (level >= lookupTable.getNumLevels() || level < 0) {
      logger.debug("num_id: " + getIdentity().getIdentifier() + " is returning a findLadder response");
      return LookupTable.EMPTY_NODE;
    }
    // If the current node and the inserted node have common bits more than the current level,
    // then this node is the neighbor so we return it
    if (target.commonPrefix(getIdentity().getMemVec()) > level) {
      logger.debug("num_id: " + getIdentity().getIdentifier() + " is returning a findLadder response");
      return this.getIdentity();
    }
    SkipNodeIdentity curr = (direction == 0) ? getLeftNode(level) : getRightNode(level);
    while (!curr.equals(LookupTable.EMPTY_NODE) && target.commonPrefix(curr.getMemVec()) <= level) {
      logger.debug("num_id: " + getIdentity().getIdentifier() + " is in findLadder loop at level " + level + " with " + curr.getIdentifier());
      // Try to find a new neighbor, but immediately return if the neighbor is locked.
      curr = (direction == 0) ? middleLayer.getLeftNeighborOf(false, curr.getAddress(), curr.getPort(), curr.getIdentifier(), level)
                              : middleLayer.getRightNeighborOf(false, curr.getAddress(), curr.getPort(), curr.getIdentifier(), level);
      // If the potential neighbor is locked, we will get an invalid identity.
      // We should directly return it in that case.
      if (curr.equals(LookupTable.INVALID_NODE)) {
        return curr;
      }
    }
    logger.debug("num_id: " + getIdentity().getIdentifier() + " is returning a findLadder response");
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
    logger.debug("num_id: " + getIdentity().getIdentifier() + " has updated its table");
    version++;
    int direction;

    if (node.getIdentifier().comparedTo(this.getIdentity().getIdentifier()) == Identifier.COMPARE_LESS) {
      direction = 0;
    } else {
      direction = 1;
    }

    int maxLevel = getIdentity().getMemVec().commonPrefix(node.getMemVec());
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
   * Search for the given identifier.
   *
   * @param targetIdentifier the target identifier.
   * @return The SkipNodeIdentity of the node with the given target identifier. If it does not exist, returns the SkipNodeIdentity of the SkipNode with NumID
   *     closest to the given numID from the direction the search is initiated. For example: Initiating a search for a SkipNode with NumID 50 from
   *     a SnipNode with NumID 10 will return the SkipNodeIdentity of the SnipNode with NumID 50 is it exists. If no such SnipNode exists, the
   *     SkipNodeIdentity of the SnipNode whose NumID is closest to 50 among the nodes whose NumID is less than 50 is returned.
   */
  @Override
  public SkipNodeIdentity searchByIdentifier(Identifier targetIdentifier) {
    // If this is the node the search request is looking for, return its identity
    if (targetIdentifier.equals(this.getIdentity().getIdentifier())) {
      return getIdentity();
    }
    // Initialize the level to begin looking at
    int level = lookupTable.getNumLevels();
    // If the target is greater than this node's numID, the search should continue to the right
    if (this.getIdentity().getIdentifier().comparedTo(targetIdentifier) == Identifier.COMPARE_LESS) {
      // Start from the top, while there is no right neighbor,
      // or the right neighbor's num ID is greater than what we are searching for keep going down
      while (level >= 0) {
        if (lookupTable.getRight(level).equals(LookupTable.EMPTY_NODE)
            || lookupTable.getRight(level).getIdentifier().comparedTo(targetIdentifier) == Identifier.COMPARE_GREATER) {
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
      return middleLayer.searchByIdentifier(delegateNode.getAddress(), delegateNode.getPort(), delegateNode.getIdentifier(), targetIdentifier);
    } else {
      // Start from the top, while there is no right neighbor,
      // or the right neighbor's num ID is greater than what we are searching for keep going down
      while (level >= 0) {
        if (lookupTable.getLeft(level).equals(LookupTable.EMPTY_NODE)
            || lookupTable.getLeft(level).getIdentifier().comparedTo(targetIdentifier) == Identifier.COMPARE_LESS) {
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
      return middleLayer.searchByIdentifier(delegateNode.getAddress(), delegateNode.getPort(), delegateNode.getIdentifier(), targetIdentifier);
    }
  }

  @Override
  public boolean isLocked() {
    return insertionLock.isLocked();
  }

  @Override
  public boolean isLockedBy(String address, int port) {
    return insertionLock.isLockedBy(address, port);
  }

  @Override
  public boolean terminate() {
    this.middleLayer.terminate();
    return true;
  }

  /**
   * Performs a membership vector lookup over the skip-graph. If the exact membership vector is not found, the most
   * similar one is returned.
   *
   * @param target the target membership vector.
   * @return the node with the membership vector most similar to the target.
   */
  @Override
  public SearchResult searchByMembershipVector(MembershipVector target) {
    if (this.getIdentity().getMemVec().equals(target)) {
      return new SearchResult(getIdentity());
    }
    // If the node is not completely inserted yet, return a tentative identity.
    if (!isAvailable()) {
      // TODO: when the node is not ready it must return not ready status.
      logger.debug("num_id: " + getIdentity().getIdentifier() + " not completely inserted yet, returning a tentative identity");
      return new SearchResult(unavailableIdentity);
    }
    // Find the level in which the search should be started from.
    int level = this.getIdentity().getMemVec().commonPrefix(target);
    if (level < 0) {
      return new SearchResult(getIdentity());
    }
    // Initiate the search.
    return middleLayer.searchByMembershipVector(getIdentity().getAddress(), getIdentity().getPort(), getIdentity().getIdentifier(), target, level);
  }

  /**
   * Recursive search by membership vector.
   *
   * @param target the target membership vector.
   * @param level  the current level.
   * @return the SkipNodeIdentity of the closest SkipNode which has the common prefix length larger than `level`.
   */
  @Override
  public SearchResult searchByMembershipVector(MembershipVector target, int level) {
    if (this.getIdentity().getMemVec().equals(target)) {
      return new SearchResult(getIdentity());
    }
    // Buffer contains the `most similar node` to return in case we cannot climb up anymore.
    // At first, we try to set this to a non-null potential ladder.
    SkipNodeIdentity left = getIdentity();
    SkipNodeIdentity right = getIdentity();
    SkipNodeIdentity buffer = (!left.equals(LookupTable.EMPTY_NODE)) ? left : right;
    if (buffer.equals(LookupTable.EMPTY_NODE)) {
      throw new IllegalStateException("the node is not completely inserted yet.");
    }
    // This loop will execute and we expand our search window until a ladder is found
    // either on the right or the left.
    while (target.commonPrefix(left.getMemVec()) <= level && target.commonPrefix(right.getMemVec()) <= level) {
      // Return the potential ladder as the result if it is the result we are looking for.
      if (left.getMemVec().equals(target)) {
        return new SearchResult(left);
      }
      if (right.getMemVec().equals(target)) {
        return new SearchResult(right);
      }
      // Expand the search window on the level.
      if (!left.equals(LookupTable.EMPTY_NODE)) {
        buffer = left;
        left = middleLayer.findLadder(left.getAddress(), left.getPort(), left.getIdentifier(), level, 0, target);
      }
      if (!right.equals(LookupTable.EMPTY_NODE)) {
        buffer = right;
        right = middleLayer.findLadder(right.getAddress(), right.getPort(), right.getIdentifier(), level, 1, target);
      }
      // Try to climb up on the either ladder.
      if (right.isNotEmpty() && target.commonPrefix(right.getMemVec()) > level) {
        level = target.commonPrefix(right.getMemVec());
        return middleLayer.searchByMembershipVector(right.getAddress(), right.getPort(), right.getIdentifier(), target, level);
      } else if (left.isNotEmpty() && target.commonPrefix(left.getMemVec()) > level) {
        level = target.commonPrefix(left.getMemVec());
        return middleLayer.searchByMembershipVector(left.getAddress(), left.getPort(), left.getIdentifier(), target, level);
      }
      // If we have expanded more than the length of the level,
      // then return the most similar node (buffer).
      if (left.isEmpty() && right.isEmpty()) {
        return new SearchResult(buffer);
      }
    }
    return new SearchResult(buffer);
  }

  @Override
  public SkipNodeIdentity updateLeftNode(SkipNodeIdentity snId, int level) {
    logger.debug(
        "num_id: " + getIdentity().getIdentifier() + " has received a updateLeftNode request with " + "skip_node_identity: " + snId + " " + "level"
            + ":" + " " + level);
    return lookupTable.updateLeft(snId, level);
  }

  @Override
  public SkipNodeIdentity updateRightNode(SkipNodeIdentity snId, int level) {
    logger.debug(
        "num_id: " + getIdentity().getIdentifier() + " has received a updateRightNode request with " + "skip_node_identity: " + snId + " " + "level"
            + ": " + level);
    return lookupTable.updateRight(snId, level);
  }

  @Override
  public SkipNodeIdentity getRightNode(int level) {
    logger.debug("num_id: " + getIdentity().getIdentifier() + " has received a getRightNode request with " + "level: " + level);
    SkipNodeIdentity right = lookupTable.getRight(level);
    SkipNodeIdentity r =
        (right.equals(LookupTable.EMPTY_NODE)) ? right : middleLayer.getIdentity(right.getAddress(), right.getPort(), right.getIdentifier());
    logger.debug("num_id: " + getIdentity().getIdentifier() + " is returning a getRightNode response");
    return r;
  }

  @Override
  public SkipNodeIdentity getLeftNode(int level) {
    logger.debug("num_id: " + getIdentity().getIdentifier() + " has received a getLeftNode request with " + "level: " + level);
    SkipNodeIdentity left = lookupTable.getLeft(level);
    SkipNodeIdentity r =
        (left.equals(LookupTable.EMPTY_NODE)) ? left : middleLayer.getIdentity(left.getAddress(), left.getPort(), left.getIdentifier());
    logger.debug("num_id: " + getIdentity().getIdentifier() + " is returning a getLeftNode response");
    return r;
  }
}
