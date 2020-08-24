package skipnode;

import lookup.TentativeTable;
import middlelayer.MiddleLayer;

import java.util.List;

public interface SkipNodeInterface {
    /**
     * Set the middle layer which would handle communication with remote nodes
     */
    void setMiddleLayer(MiddleLayer middleLayer);

    /**
     * Add the SkipNode to the SkipGraph through an introducer.
     * @param introducerAddress the address of the introducer.
     * @param introducerPort the port of the introducer.
     */
    void insert(String introducerAddress, int introducerPort);

    /**
     * Returns whether the node is available to be used as a router. If the node is still being inserted,
     * then, this will return false.
     * @return whether the node is available for routing or not.
     */
    boolean isAvailable();

    /**
     * Finds the `ladder`, i.e. the node that should be used to propagate a newly joined node to the upper layer.
     * @return the ladder's node information.
     */
    SkipNodeIdentity findLadder(int level, int direction, String target);

    /**
     * Returns the list of neighbors that the newly inserted node should have.
     * @param newNeighbor the new node.
     * @param level level of the newly inserted node.
     * @return the list of neighbors of the new node.
     */
    TentativeTable acquireNeighbors(SkipNodeIdentity newNeighbor, int level);

    /**
     * Adds the given neighbor to the appropriate lookup table entries of this node. Should only be used during concurrent
     * insertion (i.e., ConcurrentBackupTable is being used.)
     * @param newNeighbor the identity of the new neighbor.
     */
    void announceNeighbor(SkipNodeIdentity newNeighbor);

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
     * @return The SkipNodeIdentity of the SkipNode with the given nameID. If it does not exist, returns the SkipNodeIdentity of the SkipNode which shares the longest
     * prefix among the nodes in the SkipGraph
     */
    SkipNodeIdentity searchByNameID(String nameID);

    /**
     * Used by the `searchByNameID` method. Implements a recursive name ID search algorithm.
     * @param left the current left node.
     * @param right the current right node.
     * @param target the target name ID.
     * @param level the current level.
     * @return the identity of the node with the given name ID, or the node with the closest name ID.
     */
    SkipNodeIdentity searchByNameIDRecursive(SkipNodeIdentity left, SkipNodeIdentity right, String target, int level);

    /**
     * Search for the given nameID on the given level. Helper method for searchByNameID
     * @param level The level to start the search from
     * @param nameID The nameID to search for
     * @return the SkipNodeIdentity of the closest SkipNode which has the common prefix length larger than `level`.
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

    /**
     * Returns the right neighbor of the node at the given level.
     * @param level the level of the right neighbor.
     * @return the right neighbor at the given level.
     */
    SkipNodeIdentity getRightNode(int level);

    /**
     * Returns the left neighbor of the node at the given level.
     * @param level the level of the left neighbor.
     * @return the left neighbor at the given level.
     */
    SkipNodeIdentity getLeftNode(int level);

    /**
     * Returns the left ladder at the given level. Used by the search by name ID protocol. We determine
     * the eligibility of a node as a ladder by checking its availability and comparing its name ID with
     * the target name ID.
     * @param level the level.
     * @param target the target name ID of the search.
     * @return the best ladder on the left.
     */
    SkipNodeIdentity getLeftLadder(int level, String target);

    /**
     * Returns the right ladder at the given level. Used by the search by name ID protocol. We determine
     * the eligibility of a node as a ladder by checking its availability and comparing its name ID with
     * the target name ID.
     * @param level the level.
     * @param target the target name ID of the search.
     * @return the best ladder on the right.
     */
    SkipNodeIdentity getRightLadder(int level, String target);
}
