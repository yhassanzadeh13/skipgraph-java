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

        // We define the concept of a ladder. A ladder is a node that can be used to climb "up" in the skip graph.
        // During a name ID search, we can climb up when the common prefix length is higher than the node that
        // this method is called from.
        SkipNodeIdentity potentialLeftLadder = lookupTable.GetLeft(level);
        SkipNodeIdentity potentialRightLadder = lookupTable.GetRight(level);
        SkipNodeIdentity ladder = LookupTable.EMPTY_NODE;

        // We start by checking the nodes that are closest to this node from both right and left. We stop checking
        // once we find a ladder.
        while(!potentialLeftLadder.equals(LookupTable.EMPTY_NODE) || !potentialRightLadder.equals(LookupTable.EMPTY_NODE)) {
            // Check if the potential left ladder is indeed a ladder. If it is not, then try its left neighbor at the next iteration.
            int leftLadderHeight = (potentialLeftLadder.equals(LookupTable.EMPTY_NODE)) ? -1 : SkipNodeIdentity.commonBits(potentialLeftLadder.getNameID(), targetNameID);
            if(leftLadderHeight > level) {
                ladder = potentialLeftLadder;
                break;
            } else if(!potentialLeftLadder.equals(LookupTable.EMPTY_NODE)) {
                potentialLeftLadder = middleLayer.getLeftNode(potentialLeftLadder.getAddress(), potentialLeftLadder.getPort(), level);
            }
            // Check if the potential right ladder is indeed a ladder. If it is not, then try its right neighbor at the next iteration.
            int rightLadderHeight = (potentialRightLadder.equals(LookupTable.EMPTY_NODE)) ? -1 : SkipNodeIdentity.commonBits(potentialRightLadder.getNameID(), targetNameID);
            if(rightLadderHeight > level) {
                ladder = potentialRightLadder;
                break;
            } else if(!potentialRightLadder.equals(LookupTable.EMPTY_NODE)) {
                potentialRightLadder = middleLayer.getRightNode(potentialRightLadder.getAddress(), potentialRightLadder.getPort(), level);
            }
        }
        // If no ladders were found, this node is as close as we can get.
        if(ladder.equals(LookupTable.EMPTY_NODE)) {
            return getIdentity();
        }
        // If a ladder was found, delegate the search to that node.
        return middleLayer.searchByNameID(ladder.getAddress(), ladder.getPort(), targetNameID);
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
