package lookup;

import skipnode.SkipNodeIdentity;

public interface LookupTable {
    SkipNodeIdentity EMPTY_NODE = new SkipNodeIdentity("EMPTY", -1, "EMPTY", -1);

    /**
     * Updates the left neighbor on the given level to be the node
     * @param node Node to be put on the lookup table
     * @param level The level on which to insert the node
     * @return Replaced node
     */
    SkipNodeIdentity UpdateLeft(SkipNodeIdentity node, int level);

    /**
     * Updates the right neighbor on the given level to be the node
     * @param node Node to be put on the lookup table
     * @param level The level on which to insert the node
     * @return Replaced node
     */
    SkipNodeIdentity UpdateRight(SkipNodeIdentity node, int level);

    /**
     * Returns the right neighbor on the given level
     * @param level The level to get the node from
     * @return The right neighbor on the given level
     */
    SkipNodeIdentity GetRight(int level);

    /**
     * Returns the left neighbor on the given level
     * @param level The level to get the node from
     * @return The left neighbor on the given level
     */
    SkipNodeIdentity GetLeft(int level);

    /**
     * Remove the left neighbor on the given level
     * @param level The level from which to remove the left neighbor
     * @return Removed node
     */
    SkipNodeIdentity RemoveLeft(int level);

    /**
     * Remove the right neighbor on the given level
     * @param level The level from which to remove the right neighbor
     * @return Removed node
     */
    SkipNodeIdentity RemoveRight(int level);

    /**
     * Get the number of levels in the lookup table
     * @return The number of levels in the lookup table
     */
    int getNumLevels();
}
