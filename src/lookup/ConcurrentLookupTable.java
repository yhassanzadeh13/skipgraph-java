package lookup;

import skipnode.SkipNodeIdentity;

import java.util.ArrayList;
import java.util.concurrent.locks.ReadWriteLock;

/**
 * ConcurrentLookupTable is a lookup table that supports concurrent calls
 */
public class ConcurrentLookupTable implements LookupTable {
    private ReadWriteLock lock;
    private ArrayList<SkipNodeIdentity> nodes;

    private enum direction{
        LEFT,
        RIGHT
    }

    public ConcurrentLookupTable(int numLevels){
        nodes = new ArrayList<>(2*numLevels);
        for(int i=0;i<nodes.size();i++){
            nodes.set(i, LookupTable.EMPTY_NODE);
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
        SkipNodeIdentity node = nodes.get(idx);
        lock.readLock().unlock();
        return node;
    }

    @Override
    public SkipNodeIdentity GetLeft(int level) {
        lock.readLock().lock();
        int idx = getIndex(direction.LEFT, level);
        SkipNodeIdentity node = nodes.get(idx);
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

    private int getIndex(direction dir, int level){
        if(dir==direction.LEFT){
            return level*2;
        }else{
            return level*2+1;
        }
    }
}
