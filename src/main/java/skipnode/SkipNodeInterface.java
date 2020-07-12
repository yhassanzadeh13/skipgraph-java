package skipnode;

import middlelayer.MiddleLayer;

public interface SkipNodeInterface {
    /**
     * Set the middle layer which would handle communication with remote nodes
     */
    void setMiddleLayer(MiddleLayer middleLayer);

    /**
     * Add the SkipNode sn the SkipGraph through the introducer SkipNode with address introducerAddress
     * @param sn The node to insert to the SkipGraph
     * @param introducerAddress The Introducer SkipNode
     * @return True if insertion successful, false otherwise
     */
    boolean insert(SkipNodeInterface sn, String introducerAddress);

    /**
     * Remove the node from the SkipGraph. Joins the neighbors on each level together
     * @return True is successful, false otherwise
     */
    boolean delete();

    /**
     * Search for the given numID
     * @param numID The numID to search for
     * @return The SkipNodeIdentity of the node with the given numID. If it does not exist, returns the SkipNodeIdentity of the SkipNode with NumID closest to the given
     * numID from the direction the search is initiated.
     * For example: Initiating a search for a SkipNode with NumID 50 from a SnipNode with NumID 10 will return the SkipNodeIdentity of the SnipNode with NumID 50 is it exists. If
     * no such SnipNode exists, the SkipNodeIdentity of the SnipNode whose NumID is closest to 50 among the nodes whose NumID is less than 50 is returned.
     */
    SkipNodeIdentity searchByNumID(int numID);
    /**
     * Search for the given nameID
     * @param nameID The nameID to search for
     * @return The SkipNodeIdentity of the SkipNode with the given nameID. If it does not exist, returns the SkipNodeIdentity of the SnipNode which shares the longest
     * prefix among the nodes in the SkipGraph
     */
    SkipNodeIdentity searchByNameID(String nameID);

    /**
     * Search for the given nameID on the given level. Helper method for searchByNameID
     * @param level The level to start the search from
     * @param nameID The nameID to search for
     * @return The SkipNodeIdentity of the SkipNode with the given nameID. If it does not exist, returns the SkipNodeIdentity of the SnipNode which shares the longest
     * prefix among the nodes in the SkipGraph
     */
    SkipNodeIdentity nameIDLevelSearch(int level, int direction, String nameID);
    /**
     * Updates the SkipNode on the left on the given level to the given SkipNodeIdentity
     * @param snId The new SkipNodeIdentity to be placed in the given level
     * @param level The level to place the given SkipNodeIdentity
     * @return The SkipNodeIdentity that was replaced (Could be an EMPTY_NODE)
     */
    SkipNodeIdentity updateLeftNode(SkipNodeIdentity snId, int level);

    /**
     * Updates the SkipNode on the right on the given level to the given SkipNodeIdentity
     * @param snId The new SkipNodeIdentity to be placed in the given level
     * @param level The level to place the given SkipNodeIdentity
     * @return The SkipNodeIdentity that was replaced (Could be an EMPTY_NODE)
     */
    SkipNodeIdentity updateRightNode(SkipNodeIdentity snId, int level);
}
