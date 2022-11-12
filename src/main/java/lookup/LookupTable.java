package lookup;

import model.skipgraph.SkipGraph;
import skipnode.SkipNodeIdentity;

/**
 * Lookup Table interface.
 */
public interface LookupTable {
  enum Direction {
    LEFT,
    RIGHT
  }

  SkipNodeIdentity EMPTY_NODE = new SkipNodeIdentity(SkipGraph.getEmptyIdentifier(), SkipGraph.getEmptyMembershipVector(), "EMPTY", -1);
  SkipNodeIdentity INVALID_NODE = new SkipNodeIdentity(SkipGraph.getInvalidIdentifier(), SkipGraph.getEmptyMembershipVector(), "INVALID", -1);

  /**
   * Updates the left neighbor on the given level to be the node.
   *
   * @param node  Node to be put on the lookup table
   * @param level The level on which to insert the node
   * @return Replaced node
   */
  SkipNodeIdentity updateLeft(SkipNodeIdentity node, int level);

  /**
   * Updates the right neighbor on the given level to be the node.
   *
   * @param node  Node to be put on the lookup table
   * @param level The level on which to insert the node
   * @return Replaced node
   */
  SkipNodeIdentity updateRight(SkipNodeIdentity node, int level);

  /**
   * Returns the best right neighbor on the given level.
   *
   * @param level The level to get the node from
   * @return The right neighbor on the given level
   */
  SkipNodeIdentity getRight(int level);

  /**
   * Returns the best left neighbor on the given level.
   *
   * @param level The level to get the node from
   * @return The left neighbor on the given level
   */
  SkipNodeIdentity getLeft(int level);


  /**
   * Returns whether the given left neighbor exists in this lookup table at the given level.
   *
   * @param neighbor the neighbor to check existence of.
   * @param level    the level of the neighbor.
   * @return true iff the neighbor is a left neighbor at the given level.
   */
  boolean isLeftNeighbor(SkipNodeIdentity neighbor, int level);

  /**
   * Returns whether the given right neighbor exists in this lookup table at the given level.
   *
   * @param neighbor the neighbor to check existence of.
   * @param level    the level of the neighbor.
   * @return true iff the neighbor is a right neighbor at the given level.
   */
  boolean isRightNeighbor(SkipNodeIdentity neighbor, int level);

  /**
   * Get the number of levels in the lookup table.
   *
   * @return The number of levels in the lookup table
   */
  int getNumLevels();
}
