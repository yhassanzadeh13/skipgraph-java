package skipnode;

import lookup.LookupTable;
import middlelayer.MiddleLayer;

import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

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

    private boolean inserted = false;
    private final ReentrantLock insertionLock = new ReentrantLock();

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
    public void insert(String introducerAddress, int introducerPort) {
        insertionLock.lock();
        // Do not reinsert an already inserted node.
        if(inserted) return;
        // Insert the first node of the skip graph.
        if(introducerAddress == null) {
            inserted = true;
            return;
        }
        // Find my 0-level neighbor by making a num-id search through the introducer.
        SkipNodeIdentity searchResult = middleLayer.searchByNumID(introducerAddress, introducerPort, numID);
        // Inform my new neighbor about the insertion and receive my new 0-level neighbors.
        List<SkipNodeIdentity> newNeighbors = middleLayer.getPotentialNeighbors(searchResult.getAddress(), searchResult.getPort(), getIdentity(), 0);
        lookupTable.initializeNeighbors(getIdentity(), newNeighbors, 0);
        SkipNodeIdentity left = lookupTable.getLeft(0);
        SkipNodeIdentity right = lookupTable.getRight(0);
        // Announce myself to my new neighbors.
        lookupTable.getLefts(0).forEach(n -> {
            middleLayer.updateRightNode(n.getAddress(), n.getPort(), getIdentity(), 0);
        });
        lookupTable.getRights(0).forEach(n -> {
            middleLayer.updateLeftNode(n.getAddress(), n.getPort(), getIdentity(), 0);
        });
        // Now, we need to climb up according to my name ID.
        int level = 0;
        while(level < lookupTable.getNumLevels()) {
            SkipNodeIdentity ladder = LookupTable.EMPTY_NODE;
            if(!left.equals(LookupTable.EMPTY_NODE)) {
                // Get my new left node at the level.
                SkipNodeIdentity leftLadder = middleLayer.findLadder(left.getAddress(), left.getPort(), level, 0, getNameID());
                if(!leftLadder.equals(LookupTable.EMPTY_NODE)) {
                    ladder = leftLadder;
                } else {
                    left = LookupTable.EMPTY_NODE;
                    // Retry for a right ladder at the same level.
                    continue;
                }
            } else if(!right.equals(LookupTable.EMPTY_NODE)) {
                // Get my new right node at the level.
                SkipNodeIdentity rightLadder = middleLayer.findLadder(right.getAddress(), right.getPort(), level, 1, getNameID());
                if(!rightLadder.equals(LookupTable.EMPTY_NODE)) {
                    ladder = rightLadder;
                } else {
                    right = LookupTable.EMPTY_NODE;
                }
            }
            // If we were able to find a ladder, insert the node at the upper level.
            if(!ladder.equals(LookupTable.EMPTY_NODE)) {
                final int newLevel = level+1;
                // Receive my new neighbors at the level.
                newNeighbors = middleLayer.getPotentialNeighbors(ladder.getAddress(), ladder.getPort(), getIdentity(), newLevel);
                lookupTable.initializeNeighbors(getIdentity(), newNeighbors, newLevel);
                left = lookupTable.getLeft(newLevel);
                right = lookupTable.getRight(newLevel);
                // Announce myself to my new neighbors.
                lookupTable.getLefts(level+1).forEach(n -> {
                    middleLayer.updateRightNode(n.getAddress(), n.getPort(), getIdentity(), newLevel);
                });
                lookupTable.getRights(level+1).forEach(n -> {
                    middleLayer.updateLeftNode(n.getAddress(), n.getPort(), getIdentity(), newLevel);
                });
            }
            level++;
        }
        inserted = true;
        insertionLock.unlock();
    }

    /**
     * Finds the `ladder`, i.e. the node that should be used to propagate a newly joined node to the upper layer.
     * @return the `ladder` node information.
     */
    public SkipNodeIdentity findLadder(int level, int direction, String target) {
        // If the current node and the inserted node have common bits more than the current level,
        // then this node is the neighbor so we return it
        if(SkipNodeIdentity.commonBits(target, getNameID()) > level) {
            return getIdentity();
        }
        // Response from the neighbor.
        SkipNodeIdentity neighborResponse;
        // If the search is to the right...
        if(direction == 1) {
            // And if the right neighbor does not exist then at this level the right neighbor of the inserted node is null.
            if(lookupTable.getRight(level).equals(LookupTable.EMPTY_NODE)) {
                return LookupTable.EMPTY_NODE;
            }
            // Otherwise, delegate the search to right neighbor.
            SkipNodeIdentity rightNeighbor = lookupTable.getRight(level);
            neighborResponse = middleLayer.findLadder(rightNeighbor.getAddress(), rightNeighbor.getPort(), level, 1, target);
        } else {
            // If the search is to the left and if the left neighbor is null, then the left neighbor of the inserted
            // node at this level is null.
            if(lookupTable.getLeft(level).equals(LookupTable.EMPTY_NODE)) {
                return LookupTable.EMPTY_NODE;
            }
            // Otherwise, delegate the search to the left neighbor.
            SkipNodeIdentity leftNeighbor = lookupTable.getLeft(level);
            neighborResponse = middleLayer.findLadder(leftNeighbor.getAddress(), leftNeighbor.getPort(), level, 0, target);
        }
        return neighborResponse;
    }

    @Override
    public List<SkipNodeIdentity> getPotentialNeighbors(SkipNodeIdentity newNeighbor, int level) {
        // Get the potential neighbors of the new node at this level.
        return lookupTable.getPotentialNeighbors(getIdentity(), newNeighbor.getNumID(), level);
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
        if (this.numID < numID) {
            // Start from the top, while there is no right neighbor, or the right neighbor's num ID is greater than what we are searching for
            // keep going down
            while(level>=0) {
                if (lookupTable.getRight(level)==LookupTable.EMPTY_NODE ||
                        lookupTable.getRight(level).getNumID() > numID){
                    level--;
                } else {
                    break;
                }
            }
            // If the level is less than zero, then this node is the closest node to the numID being searched for from the right. Return.
            if (level < 0) {
                return getIdentity();
            }
            // Else, delegate the search to that node on the right
            SkipNodeIdentity delegateNode = lookupTable.getRight(level);
            return middleLayer.searchByNumID(delegateNode.getAddress(), delegateNode.getPort(), numID);
        } else {
            // Start from the top, while there is no right neighbor, or the right neighbor's num ID is greater than what we are searching for
            // keep going down
            while(level>=0) {
                if (lookupTable.getLeft(level)==LookupTable.EMPTY_NODE ||
                        lookupTable.getLeft(level).getNumID() < numID){
                    level--;
                } else {
                    break;
                }
            }
            // If the level is less than zero, then this node is the closest node to the numID being searched for from the right. Return.
            if (level < 0) {
                return getIdentity();
            }
            // Else, delegate the search to that node on the right
            SkipNodeIdentity delegateNode = lookupTable.getLeft(level);
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
            // Return the potential ladder as the result if it is the result we are looking for.
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
        return lookupTable.updateLeft(snId, level);
    }

    @Override
    public SkipNodeIdentity updateRightNode(SkipNodeIdentity snId, int level) {
        return lookupTable.updateRight(snId, level);
    }

    @Override
    public SkipNodeIdentity getRightNode(int level) {
        return lookupTable.getRight(level);
    }

    @Override
    public SkipNodeIdentity getLeftNode(int level) {
        return lookupTable.getLeft(level);
    }

}
