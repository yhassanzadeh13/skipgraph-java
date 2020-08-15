package lookup;

import skipnode.SkipNodeIdentity;

import java.util.ArrayList;
import java.util.List;
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

    public ConcurrentLookupTable(int numLevels) {
        this.numLevels = numLevels;
        lock = new ReentrantReadWriteLock(true);
        nodes = new ArrayList<>(2*numLevels);
        for(int i = 0; i < 2 * numLevels; i++){
            nodes.add(i, LookupTable.EMPTY_NODE);
        }
    }

    @Override
    public SkipNodeIdentity updateLeft(SkipNodeIdentity node, int level) {
        lock.writeLock().lock();
        int idx = getIndex(direction.LEFT, level);
        if(idx >= nodes.size()) return LookupTable.EMPTY_NODE;
        SkipNodeIdentity prev = nodes.set(idx,node);
        lock.writeLock().unlock();
        return prev;
    }

    @Override
    public SkipNodeIdentity updateRight(SkipNodeIdentity node, int level) {
        lock.writeLock().lock();
        int idx = getIndex(direction.RIGHT, level);
        if(idx >= nodes.size()) return LookupTable.EMPTY_NODE;
        SkipNodeIdentity prev = nodes.set(idx,node);
        lock.writeLock().unlock();
        return prev;
    }

    @Override
    public SkipNodeIdentity getRight(int level) {
        lock.readLock().lock();
        int idx = getIndex(direction.RIGHT, level);
        SkipNodeIdentity node = (idx < nodes.size()) ? nodes.get(idx) : LookupTable.EMPTY_NODE;
        lock.readLock().unlock();
        return node;
    }

    @Override
    public SkipNodeIdentity getLeft(int level) {
        lock.readLock().lock();
        int idx = getIndex(direction.LEFT, level);
        SkipNodeIdentity node = (idx < nodes.size()) ? nodes.get(idx) : LookupTable.EMPTY_NODE;
        lock.readLock().unlock();
        return node;
    }

    @Override
    public List<SkipNodeIdentity> getRights(int level) {
        List<SkipNodeIdentity> ls = new ArrayList<>(1);
        ls.add(getRight(level));
        return ls;
    }

    @Override
    public List<SkipNodeIdentity> getLefts(int level) {
        List<SkipNodeIdentity> ls = new ArrayList<>(1);
        ls.add(getLeft(level));
        return ls;
    }

    @Override
    public boolean isLeftNeighbor(SkipNodeIdentity neighbor, int level) {
        lock.readLock().lock();
        boolean exists = getLeft(level).equals(neighbor);
        lock.readLock().unlock();
        return exists;
    }

    @Override
    public boolean isRightNeighbor(SkipNodeIdentity neighbor, int level) {
        lock.readLock().lock();
        boolean exists = getRight(level).equals(neighbor);
        lock.readLock().unlock();
        return exists;
    }

    @Override
    public SkipNodeIdentity removeLeft(int level) {
        return updateLeft(LookupTable.EMPTY_NODE, level);
    }

    @Override
    public SkipNodeIdentity removeRight(int level) {
        return updateRight(LookupTable.EMPTY_NODE, level);
    }

    @Override
    public int getNumLevels() {
        return this.numLevels;
    }

    @Override
    public List<SkipNodeIdentity> getPotentialNeighbors(SkipNodeIdentity owner, int newNumID, int level) {
        lock.readLock().lock();
        List<SkipNodeIdentity> potentialNeighbors = new ArrayList<>();
        potentialNeighbors.add(owner);
        if(newNumID < owner.getNumID() && !getLeft(level).equals(LookupTable.EMPTY_NODE))
            potentialNeighbors.add(getLeft(level));
        else if(!getRight(level).equals(LookupTable.EMPTY_NODE))
            potentialNeighbors.add(getRight(level));
        lock.readLock().unlock();
        return potentialNeighbors;
    }

    // Given a set of new neighbors, puts them into the correct place (left or right).
    @Override
    public void initializeNeighbors(SkipNodeIdentity owner, List<SkipNodeIdentity> potentialNeighbors, int level) {
        SkipNodeIdentity left = potentialNeighbors.stream()
                .filter(x -> x.getNumID() <= owner.getNumID())
                .findFirst()
                .orElse(LookupTable.EMPTY_NODE);
        SkipNodeIdentity right = potentialNeighbors.stream()
                .filter(x -> x.getNumID() > owner.getNumID())
                .findFirst()
                .orElse(LookupTable.EMPTY_NODE);
        updateLeft(left, level);
        updateRight(right, level);
    }

    private int getIndex(direction dir, int level) {
        if(level < 0) return Integer.MAX_VALUE;
        if(dir==direction.LEFT) {
            return level * 2;
        }else{
            return level * 2 + 1;
        }
    }
}
