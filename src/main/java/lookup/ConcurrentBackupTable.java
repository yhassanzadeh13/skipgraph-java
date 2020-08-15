package lookup;
import skipnode.SkipNodeIdentity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

/**
 * ConcurrentLookupTable is a backup table that supports concurrent calls
 */
public class ConcurrentBackupTable implements LookupTable {

    private final int numLevels;
    private final int maxSize;
    private final ReadWriteLock lock;
    /**
     * All the neighbors are placed in an arraylist, with EMPTY_NODE for empty nodes.
     * The formula to get the index of a neighbor is 2*level for a node on the left side
     * and 2*level+1 for a node on the right side. This is reflected in the getIndex
     * method.
     */
    private final ArrayList<List<SkipNodeIdentity>> nodes;
    private final List<SkipNodeIdentity> emptyLevel = new ArrayList<>();

    private enum direction {
        LEFT,
        RIGHT
    }

    public ConcurrentBackupTable(int numLevels, int maxSize) {
        this.numLevels = numLevels;
        this.maxSize = maxSize;
        lock = new ReentrantReadWriteLock(true);
        nodes = new ArrayList<>(2 * numLevels);
        for(int i = 0; i < 2 * numLevels; i++) {
            // At each lookup table entry, we store a list of nodes instead of a single node.
            nodes.add(i, new ArrayList<>(maxSize));
        }
    }

    public ConcurrentBackupTable(int numLevels) {
        this(numLevels, 100);
    }

    @Override
    public SkipNodeIdentity updateLeft(SkipNodeIdentity node, int level) {
        addLeftNode(node, level);
        return getLeft(level);
    }

    @Override
    public SkipNodeIdentity updateRight(SkipNodeIdentity node, int level) {
        addRightNode(node, level);
        return getRight(level);
    }

    @Override
    public List<SkipNodeIdentity> getRights(int level) {
        lock.readLock().lock();
        int idx = getIndex(direction.RIGHT, level);
        if(idx >= nodes.size()){
            return emptyLevel;
        }
        // This works because SkipNodeIdentity is immutable.
        ArrayList<SkipNodeIdentity> result = new ArrayList<>(nodes.get(idx));
        lock.readLock().unlock();
        return result;
    }

    @Override
    public List<SkipNodeIdentity> getLefts(int level) {
        lock.readLock().lock();
        int idx = getIndex(direction.LEFT, level);
        if(idx >= nodes.size()){
            lock.readLock().unlock();
            return emptyLevel;
        }
        // This works because SkipNodeIdentity is immutable.
        ArrayList<SkipNodeIdentity> result = new ArrayList<>(nodes.get(idx));
        lock.readLock().unlock();
        return result;
    }

    @Override
    public SkipNodeIdentity getRight(int level) {
        lock.readLock().lock();
        int idx = getIndex(direction.RIGHT, level);
        SkipNodeIdentity node = LookupTable.EMPTY_NODE;
        // If we have a non-empty backup list at the index, return the first
        // element of the backup list.
        if(idx < nodes.size() && nodes.get(idx).size() > 0) {
            node = nodes.get(idx).get(0);
        }
        lock.readLock().unlock();
        return node;
    }

    @Override
    public SkipNodeIdentity getLeft(int level) {
        lock.readLock().lock();
        int idx = getIndex(direction.LEFT, level);
        SkipNodeIdentity node = LookupTable.EMPTY_NODE;
        // If we have a non-empty backup list at the index, return the first
        // element of the backup list.
        if(idx < nodes.size() && nodes.get(idx).size() > 0) {
            node = nodes.get(idx).get(0);
        }
        lock.readLock().unlock();
        return node;
    }

    @Override
    public boolean isLeftNeighbor(SkipNodeIdentity neighbor, int level) {
        return getLefts(level).stream().anyMatch(x -> x.equals(neighbor));
    }

    @Override
    public boolean isRightNeighbor(SkipNodeIdentity neighbor, int level) {
        return getRights(level).stream().anyMatch(x -> x.equals(neighbor));
    }

    public List<SkipNodeIdentity> addRightNode(SkipNodeIdentity node, int level) {
        lock.writeLock().lock();
        int idx = getIndex(direction.RIGHT, level);
        List<SkipNodeIdentity> entry = nodes.get(idx);
        entry.add(node);
        // Sort the node list in ascending order.
        Collections.sort(entry);
        // Remove the last node if it exceeds the max node list size.
        if(entry.size() > this.maxSize) {
            entry.remove(entry.size()-1);
        }
        lock.writeLock().unlock();
        return entry;
    }

    public List<SkipNodeIdentity> addLeftNode(SkipNodeIdentity node, int level) {
        lock.writeLock().lock();
        int idx = getIndex(direction.LEFT, level);
        List<SkipNodeIdentity> entry = nodes.get(idx);
        entry.add(node);
        // Sort the node list in a descending order.
        Collections.sort(entry);
        Collections.reverse(entry);
        // Remove the latest node if it exceeds the max node list size.
        if(entry.size() > this.maxSize) {
            entry.remove(entry.size()-1);
        }
        lock.writeLock().unlock();
        return entry;
    }

    @Override
    public SkipNodeIdentity removeLeft(int level) {
        SkipNodeIdentity lft = getLeft(level);
        removeLeft(lft, level);
        return getLeft(level);
    }

    @Override
    public SkipNodeIdentity removeRight(int level) {
        SkipNodeIdentity right = getRight(level);
        removeRight(right, level);
        return getRight(level);
    }

    public List<SkipNodeIdentity> removeLeft(SkipNodeIdentity sn, int level) {
        lock.writeLock().lock();
        List<SkipNodeIdentity> leftNodes = getLefts(level);
        leftNodes.removeIf(nd -> nd.equals(sn));
        lock.writeLock().unlock();
        return leftNodes;
    }

    public List<SkipNodeIdentity> removeRight(SkipNodeIdentity sn, int level) {
        lock.writeLock().lock();
        List<SkipNodeIdentity> rightNodes = getRights(level);
        rightNodes.removeIf(nd -> nd.equals(sn));
        lock.writeLock().unlock();
        return rightNodes;
    }

    @Override
    public List<SkipNodeIdentity> getPotentialNeighbors(SkipNodeIdentity owner, int newNumID, int level) {
        lock.readLock().lock();
        List<SkipNodeIdentity> potentialNeighbors = new ArrayList<>();
        potentialNeighbors.add(owner);
        potentialNeighbors.addAll(getLefts(level));
        potentialNeighbors.addAll(getRights(level));
        lock.readLock().unlock();
        return potentialNeighbors;
    }

    @Override
    public void initializeNeighbors(SkipNodeIdentity owner, List<SkipNodeIdentity> potentialNeighbors, int level) {
        List<SkipNodeIdentity> leftList = potentialNeighbors.stream()
                .filter(x -> x.getNumID() <= owner.getNumID())
                .sorted()
                .collect(Collectors.toList());
        List<SkipNodeIdentity> rightList = potentialNeighbors.stream()
                .filter(x -> x.getNumID() > owner.getNumID())
                .sorted()
                .collect(Collectors.toList());
        // Left neighbors should be in descending order.
        Collections.reverse(leftList);
        lock.writeLock().lock();
        // Add the neighbors at the appropriate lists.
        int l = getIndex(direction.LEFT, level);
        int r = getIndex(direction.RIGHT, level);
        nodes.get(l).addAll(leftList);
        nodes.get(r).addAll(rightList);
        lock.writeLock().unlock();
    }

    @Override
    public int getNumLevels() {
        return this.numLevels;
    }

    private int getIndex(direction dir, int level){
        if(level<0) return Integer.MAX_VALUE;
        if(dir==direction.LEFT){
            return level*2;
        }else{
            return level*2+1;
        }
    }
}

