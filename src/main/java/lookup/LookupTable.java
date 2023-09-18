package lookup;

import model.identifier.Identity;
import model.skipgraph.SkipGraph;

/**
 * Lookup Table interface.
 */
public interface LookupTable {
  Identity EMPTY_NODE = new Identity(SkipGraph.getEmptyIdentifier(), SkipGraph.getEmptyMembershipVector(), "EMPTY", -1);
  Identity INVALID_NODE = new Identity(SkipGraph.getInvalidIdentifier(), SkipGraph.getEmptyMembershipVector(), "INVALID", -1);

  /**
   * Updates the left neighbor on the given level to be the node.
   *
   * @param node  Node to be put on the lookup table
   * @param level The level on which to insert the node
   * @return Replaced node
   */
  Identity updateLeft(Identity node, int level);

  /**
   * Updates the right neighbor on the given level to be the node.
   *
   * @param node  Node to be put on the lookup table
   * @param level The level on which to insert the node
   * @return Replaced node
   */
  Identity updateRight(Identity node, int level);

  /**
   * Returns the best right neighbor on the given level.
   *
   * @param level The level to get the node from
   * @return The right neighbor on the given level
   */
  Identity getRight(int level);

  /**
   * Returns the best left neighbor on the given level.
   *
   * @param level The level to get the node from
   * @return The left neighbor on the given level
   */
  Identity getLeft(int level);

  /**
   * Returns whether the given left neighbor exists in this lookup table at the given level.
   *
   * @param neighbor the neighbor to check existence of.
   * @param level    the level of the neighbor.
   * @return true iff the neighbor is a left neighbor at the given level.
   */
  boolean isLeftNeighbor(Identity neighbor, int level);

  /**
   * Returns whether the given right neighbor exists in this lookup table at the given level.
   *
   * @param neighbor the neighbor to check existence of.
   * @param level    the level of the neighbor.
   * @return true iff the neighbor is a right neighbor at the given level.
   */
  boolean isRightNeighbor(Identity neighbor, int level);

  /**
   * Get the number of levels in the lookup table.
   *
   * @return The number of levels in the lookup table
   */
  int getNumLevels();

  /**
   * Direction of the neighbor; Left or Right.
   */
  enum Direction {
    LEFT, RIGHT
  }
}
