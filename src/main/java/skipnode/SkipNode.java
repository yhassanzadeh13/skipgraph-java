package skipnode;

import lookup.LookupTable;
import middlelayer.MiddleLayer;
import underlay.Underlay;

public class SkipNode implements SkipNodeInterface {
    /**
     * Attributes
     */
    private String address;
    private int numID;
    private String nameID;
    private LookupTable lookupTable;

    private MiddleLayer middleLayer;


    public SkipNode(SkipNodeIdentity snID, LookupTable lookupTable){
        this.address = snID.getAddress();
        this.numID = snID.getNumID();
        this.nameID = snID.getNameID();
        this.lookupTable = lookupTable;
    }


    @Override
    public void setMiddleLayer(MiddleLayer middleLayer) {
        this.middleLayer=middleLayer;
    }

    @Override
    public boolean insert(SkipNodeInterface sn, String introducerAddress) {
        //TODO Implement
        return false;
    }

    @Override
    public boolean delete() {
        //TODO Implement
        return false;
    }

    @Override
    public SkipNodeIdentity searchByNumID(int numID) {
        //TODO Implement
        return LookupTable.EMPTY_NODE;
    }

    @Override
    public SkipNodeIdentity searchByNameID(String nameID) {
        //TODO Implement
        return LookupTable.EMPTY_NODE;
    }

    @Override
    public SkipNodeIdentity nameIDLevelSearch(int level, int direction, String nameID) {
        //TODO Implement
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
}
