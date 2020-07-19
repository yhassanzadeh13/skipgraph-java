package skipnode;

import lookup.LookupTable;
import middlelayer.MiddleLayer;
import underlay.Underlay;

public class SkipNode implements SkipNodeInterface {
    /**
     * Attributes
     */
    private final String address;
    private final int port;
    private final int numID;
    private final String nameID;
    private final LookupTable lookupTable;

    private MiddleLayer middleLayer;

    public SkipNode(SkipNodeIdentity snID, LookupTable lookupTable) {
        this.address = snID.getAddress();
        this.port = snID.getPort();
        this.numID = snID.getNumID();
        this.nameID = snID.getNameID();
        this.lookupTable = lookupTable;
    }

    public int getNumID() {
        return numID;
    }

    public String getNameID() {
        return nameID;
    }

    public LookupTable getLookupTable() {
        return lookupTable;
    }

    public SkipNodeIdentity getIdentity() {
        return new SkipNodeIdentity(nameID, numID, address, port);
    }

    @Override
    public void setMiddleLayer(MiddleLayer middleLayer) {
        this.middleLayer=middleLayer;
    }

    @Override
    public boolean insert(SkipNodeInterface sn, String introducerAddress) {
        // TODO Implement
        return false;
    }

    @Override
    public boolean delete() {
        // TODO Implement
        return false;
    }

    @Override
    public SkipNodeIdentity searchByNumID(int numID) {
        // If this is the node the search request is looking for, return its identity
        if (numID == this.numID) {
            return getIdentity();
        }

        // Initialize the level to begin looking at
        int level = lookupTable.getNumLevels();

        // If the target is greater than this node's numID, the search should continue to the right
        if (this.numID < numID){
            // Start from the top, while there is no right neighbor, or the right neighbor's num ID is greater than what we are searching for
            // keep going down
            while(level>=0){
                if (lookupTable.GetRight(level)==LookupTable.EMPTY_NODE ||
                        lookupTable.GetRight(level).getNumID() > numID){
                    level--;
                }else{
                    break;
                }
            }

            // If the level is less than zero, then this node is the closest node to the numID being searched for from the right. Return.
            if (level < 0) {
                return getIdentity();
            }

            // Else, delegate the search to that node on the right
            SkipNodeIdentity delegateNode = lookupTable.GetRight(level);
            return middleLayer.searchByNumID(delegateNode.getAddress(), delegateNode.getPort(), numID);
        }else{
            // Start from the top, while there is no right neighbor, or the right neighbor's num ID is greater than what we are searching for
            // keep going down
            while(level>=0){
                if (lookupTable.GetLeft(level)==LookupTable.EMPTY_NODE ||
                        lookupTable.GetLeft(level).getNumID() < numID){
                    level--;
                }else{
                    break;
                }
            }

            // If the level is less than zero, then this node is the closest node to the numID being searched for from the right. Return.
            if (level < 0) {
                return getIdentity();
            }

            // Else, delegate the search to that node on the right
            SkipNodeIdentity delegateNode = lookupTable.GetLeft(level);
            return middleLayer.searchByNumID(delegateNode.getAddress(), delegateNode.getPort(), numID);
        }
    }

    /**
     * Performs a name ID lookup over the skip-graph. If the exact name ID is not found, the most similar one is
     * returned.
     * @param targetNameID the target name ID.
     * @return the node with the name ID most similar to the target name ID.
     */
    @Override
    public SkipNodeIdentity searchByNameID(String targetNameID) {
        if(nameID.equals(targetNameID)) return getIdentity();

        // Find the level in which the search should be started from.
        int level = SkipNodeIdentity.commonBits(nameID, targetNameID);
        if(level < 0) return getIdentity();

        // Initiate the search.
        return middleLayer.searchByNameIDRecursive(address, port, getIdentity(), getIdentity(), targetNameID, level);
    }

    /**
     * Implements the recursive search by name ID procedure.
     * @param potentialLeftLadder the left node that we potentially can utilize to climb up.
     * @param potentialRightLadder the right node that we potentially can utilize to climb up.
     * @param targetNameID the target name ID.
     * @param level the current level.
     * @return the SkipNodeIdentity of the closest SkipNode which has the common prefix length larger than `level`.
     */
    @Override
    public SkipNodeIdentity searchByNameIDRecursive(SkipNodeIdentity potentialLeftLadder, SkipNodeIdentity potentialRightLadder,
                                                    String targetNameID, int level) {
        if(nameID.equals(targetNameID)) return getIdentity();
        // Buffer contains the `most similar node` to return in case we cannot climb up anymore. At first, we try to set this to the
        // non null potential ladder.
        SkipNodeIdentity buffer = (!potentialLeftLadder.equals(LookupTable.EMPTY_NODE)) ? potentialLeftLadder : potentialRightLadder;
        // This loop will execute and we expand our search window until a ladder is found either on the right or the left.
        while(SkipNodeIdentity.commonBits(targetNameID, potentialLeftLadder.getNameID()) <= level
                && SkipNodeIdentity.commonBits(targetNameID, potentialRightLadder.getNameID()) <= level) {
            // Return the ladder as the result if it is the result we are looking for.
            if(potentialLeftLadder.getNameID().equals(targetNameID)) return potentialLeftLadder;
            if(potentialRightLadder.getNameID().equals(targetNameID)) return potentialRightLadder;
            // Expand the search window on the level.
            if(!potentialLeftLadder.equals(LookupTable.EMPTY_NODE)) {
                buffer = potentialLeftLadder;
                potentialLeftLadder = middleLayer.getLeftNode(potentialLeftLadder.getAddress(), potentialLeftLadder.getPort(), level);
            }
            if(!potentialRightLadder.equals(LookupTable.EMPTY_NODE)) {
                buffer = potentialRightLadder;
                potentialRightLadder = middleLayer.getRightNode(potentialRightLadder.getAddress(), potentialRightLadder.getPort(), level);
            }
            // Try to climb up on the either ladder.
            if(SkipNodeIdentity.commonBits(targetNameID, potentialRightLadder.getNameID()) > level) {
                level = SkipNodeIdentity.commonBits(targetNameID, potentialRightLadder.getNameID());
                SkipNodeIdentity newLeft = middleLayer.getLeftNode(potentialRightLadder.getAddress(), potentialRightLadder.getPort(), level);
                SkipNodeIdentity newRight = middleLayer.getRightNode(potentialRightLadder.getAddress(), potentialRightLadder.getPort(), level);
                return middleLayer.searchByNameIDRecursive(potentialRightLadder.getAddress(), potentialRightLadder.getPort(),
                        newLeft, newRight, targetNameID, level);
            } else if(SkipNodeIdentity.commonBits(targetNameID, potentialLeftLadder.getNameID()) > level) {
                level = SkipNodeIdentity.commonBits(targetNameID, potentialLeftLadder.getNameID());
                SkipNodeIdentity newLeft = middleLayer.getLeftNode(potentialLeftLadder.getAddress(), potentialLeftLadder.getPort(), level);
                SkipNodeIdentity newRight = middleLayer.getRightNode(potentialLeftLadder.getAddress(), potentialLeftLadder.getPort(), level);
                return middleLayer.searchByNameIDRecursive(potentialLeftLadder.getAddress(), potentialLeftLadder.getPort(),
                        newLeft, newRight, targetNameID, level);
            }
            // If we have expanded more than the length of the level, then return the most similar node (buffer).
            if(potentialLeftLadder.equals(LookupTable.EMPTY_NODE) && potentialRightLadder.equals(LookupTable.EMPTY_NODE)) {
                return buffer;
            }
        }
        return buffer;
    }

    @Override
    public SkipNodeIdentity nameIDLevelSearch(int level, int direction, String nameID) {
        // TODO Implement
        return LookupTable.EMPTY_NODE;
    }

    @Override
    public SkipNodeIdentity updateLeftNode(SkipNodeIdentity snId, int level) {
        return lookupTable.UpdateLeft(snId, level);
    }

    @Override
    public SkipNodeIdentity updateRightNode(SkipNodeIdentity snId, int level) {
        return lookupTable.UpdateRight(snId, level);
    }

    @Override
    public SkipNodeIdentity getRightNode(int level) {
        return lookupTable.GetRight(level);
    }

    @Override
    public SkipNodeIdentity getLeftNode(int level) {
        return lookupTable.GetLeft(level);
    }

}
