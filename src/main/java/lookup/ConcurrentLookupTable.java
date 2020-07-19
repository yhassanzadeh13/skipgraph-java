package lookup;

import skipnode.SkipNodeIdentity;

import java.util.ArrayList;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * ConcurrentLookupTable is a lookup table that supports concurrent calls
 */
public class ConcurrentLookupTable implements LookupTable {
    private final int numLevels;
    private ReadWriteLock lock;
    /**
     * All the neighbors are placed in an arraylist, with EMPTY_NODE for empty nodes.
     * The formula to get the index of a neighbor is 2*level for a node on the left side
     * and 2*level+1 for a node on the right side. This is reflected in the getIndex
     * method.
     */
    private ArrayList<SkipNodeIdentity> nodes;

    private enum direction{
        LEFT,
        RIGHT
    }

    public ConcurrentLookupTable(int numLevels){
        this.numLevels=numLevels;
        lock = new ReentrantReadWriteLock(true);
        nodes = new ArrayList<>(2*numLevels);
        for(int i=0;i<2*numLevels;i++){
            nodes.add(i, LookupTable.EMPTY_NODE);
        }
    }

    @Override
    public SkipNodeIdentity UpdateLeft(SkipNodeIdentity node, int level) {
        lock.writeLock().lock();
        int idx = getIndex(direction.LEFT, level);
        SkipNodeIdentity prev = nodes.set(idx,node);
        lock.writeLock().unlock();
        return prev;
    }

    @Override
    public SkipNodeIdentity UpdateRight(SkipNodeIdentity node, int level) {
        lock.writeLock().lock();
        int idx = getIndex(direction.RIGHT, level);
        SkipNodeIdentity prev = nodes.set(idx,node);
        lock.writeLock().unlock();
        return prev;
    }

    @Override
    public SkipNodeIdentity GetRight(int level) {
        lock.readLock().lock();
        int idx = getIndex(direction.RIGHT, level);
        SkipNodeIdentity node = (idx < nodes.size()) ? nodes.get(idx) : LookupTable.EMPTY_NODE;
        lock.readLock().unlock();
        return node;
    }

    @Override
    public SkipNodeIdentity GetLeft(int level) {
        lock.readLock().lock();
        int idx = getIndex(direction.LEFT, level);
        SkipNodeIdentity node = (idx < nodes.size()) ? nodes.get(idx) : LookupTable.EMPTY_NODE;
        lock.readLock().unlock();
        return node;
    }

    @Override
    public SkipNodeIdentity RemoveLeft(int level) {
        return UpdateLeft(LookupTable.EMPTY_NODE, level);
    }

    @Override
    public SkipNodeIdentity RemoveRight(int level) {
        return UpdateRight(LookupTable.EMPTY_NODE, level);
    }

    @Override
    public int getNumLevels() {
        return 0;
    }

    private int getIndex(direction dir, int level){
        if(dir==direction.LEFT){
            return level*2;
        }else{
            return level*2+1;
        }
    }
}
