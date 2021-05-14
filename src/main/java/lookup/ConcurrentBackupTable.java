package lookup;
import log.Log4jLogger;
import org.apache.logging.log4j.LogManager;
import skipnode.SkipNodeIdentity;

import java.util.*;
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
    private final SkipNodeIdentity owner;
    private final ArrayList<List<SkipNodeIdentity>> nodes;
    private final List<SkipNodeIdentity> emptyLevel = new ArrayList<>();

    private static final Log4jLogger logger = new Log4jLogger(LogManager.getLogger(ConcurrentBackupTable.class));


    private enum direction {
        LEFT,
        RIGHT
    }

    public ConcurrentBackupTable(int numLevels, int maxSize, SkipNodeIdentity owner) {
        this.numLevels = numLevels;
        this.maxSize = maxSize;
        this.owner = owner;
        lock = new ReentrantReadWriteLock(true);
        nodes = new ArrayList<>(2 * numLevels);
        for(int i = 0; i < 2 * numLevels; i++) {
            // At each lookup table entry, we store a list of nodes instead of a single node.
            nodes.add(i, new ArrayList<>(maxSize));
        }
    }

    public ConcurrentBackupTable(int numLevels, SkipNodeIdentity owner) {
        this(numLevels, 100, owner);
    }

    @Override
    public SkipNodeIdentity updateLeft(SkipNodeIdentity node, int level) {
        logger.debug().
                Int("owner_num_id", owner.getNumID()).
                Int("num_id", node.getNumID()).
                Int("level", level).
                Msg("updating left");
        addLeftNode(node, level);
        return getLeft(level);
    }

    @Override
    public SkipNodeIdentity updateRight(SkipNodeIdentity node, int level) {
        logger.debug().
                Int("owner_num_id", owner.getNumID()).
                Int("num_id", node.getNumID()).
                Int("level", level).
                Msg("updating right");
        addRightNode(node, level);
        return getRight(level);
    }

    @Override
    public List<SkipNodeIdentity> getRights(int level) {
        lock.readLock().lock();
        int idx = getIndex(direction.RIGHT, level);
        logger.debug().
                Int("owner_num_id", owner.getNumID()).
                Int("level", level).
                Int("idx", idx).
                Msg("getting rights");
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
        logger.debug().
                Int("owner_num_id", owner.getNumID()).
                Int("level", level).
                Int("idx", idx).
                Msg("getting lefts");
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
        logger.debug().
                Int("owner_num_id", owner.getNumID()).
                Int("level", level).
                Int("idx", idx).
                Msg("getting right");
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
        logger.debug().
                Int("owner_num_id", owner.getNumID()).
                Int("level", level).
                Int("idx", idx).
                Msg("getting left");
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
        int trial = 1;
        // Exponential backoff for writing.
        while(!lock.writeLock().tryLock()) {
            int sleepTime = (int) (Math.random() * Math.pow(2, trial-1) * 50);
            try {
                Thread.sleep(sleepTime);
            } catch (InterruptedException e) {
                logger.fatal().
                        Exception(e).
                        Int("owner_num_id", owner.getNumID()).
                        Int("num_id", node.getNumID()).
                        Int("level", level).
                        Int("sleep_time", sleepTime).
                        Int("trial", trial).
                        Msg("could not backoff");
            }
            trial++;
        }
        int idx = getIndex(direction.RIGHT, level);
        List<SkipNodeIdentity> entry = nodes.get(idx);
        logger.debug().
                Int("owner_num_id", owner.getNumID()).
                Int("num_id", node.getNumID()).
                Int("level", level).
                Int("idx", idx).
                Msg("adding right node");
        entry.add(node);
        // Sort the node list in ascending order.
        Collections.sort(entry);
        // Remove the last node if it exceeds the max node list size.
        if(entry.size() > this.maxSize) {
            logger.debug().
                    Int("owner_num_id", owner.getNumID()).
                    Int("num_id", node.getNumID()).
                    Int("level", level).
                    Int("max_size", maxSize).
                    Msg("removing last node");
            entry.remove(entry.size()-1);
        }
        lock.writeLock().unlock();
        return entry;
    }

    public List<SkipNodeIdentity> addLeftNode(SkipNodeIdentity node, int level) {
        int trial = 1;
        // Exponential backoff for writing.
        while(!lock.writeLock().tryLock()) {
            int sleepTime = (int) (Math.random() * Math.pow(2, trial) * 50);
            try {
                Thread.sleep(sleepTime);
            } catch (InterruptedException e) {
                logger.fatal().
                        Exception(e).
                        Int("owner_num_id", owner.getNumID()).
                        Int("num_id", node.getNumID()).
                        Int("level", level).
                        Int("sleep_time", sleepTime).
                        Int("trial", trial).
                        Msg("could not backoff");
            }
            trial++;
        }
        int idx = getIndex(direction.LEFT, level);
        List<SkipNodeIdentity> entry = nodes.get(idx);
        logger.debug().
                Int("owner_num_id", owner.getNumID()).
                Int("num_id", node.getNumID()).
                Int("level", level).
                Int("idx", idx).
                Msg("adding left node");
        entry.add(node);
        // Sort the node list in a descending order.
        Collections.sort(entry);
        Collections.reverse(entry);
        // Remove the latest node if it exceeds the max node list size.
        if(entry.size() > this.maxSize) {
            logger.debug().
                    Int("owner_num_id", owner.getNumID()).
                    Int("num_id", node.getNumID()).
                    Int("level", level).
                    Int("max_size", maxSize).
                    Msg("removing last node");
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
        logger.debug().
                Int("owner_num_id", owner.getNumID()).
                Int("num_id", sn.getNumID()).
                Int("level", level).
                Msg("removing left");
        List<SkipNodeIdentity> leftNodes = getLefts(level);
        leftNodes.removeIf(nd -> nd.equals(sn));
        lock.writeLock().unlock();
        return leftNodes;
    }

    public List<SkipNodeIdentity> removeRight(SkipNodeIdentity sn, int level) {
        lock.writeLock().lock();
        logger.debug().
                Int("owner_num_id", owner.getNumID()).
                Int("num_id", sn.getNumID()).
                Int("level", level).
                Msg("removing right");
        List<SkipNodeIdentity> rightNodes = getRights(level);
        rightNodes.removeIf(nd -> nd.equals(sn));
        lock.writeLock().unlock();
        return rightNodes;
    }

    /**
     * Returns the new neighbors (unsorted) of a newly inserted node. It is assumed that the newly inserted node
     * will be a neighbor to the owner of this lookup table.
     * @param newNameID the name ID of the newly inserted node.
     * @param newNumID the num ID of the newly inserted node.
     * @param level the level of the new neighbor.
     * @return the list of neighbors (both right and left) of the newly inserted node.
     */
    @Override
    public TentativeTable acquireNeighbors(int newNumID, String newNameID, int level) {
        lock.readLock().lock();
        logger.debug().
                Int("owner_num_id", owner.getNumID()).
                Int("new_num_id", newNumID).
                Str("new_name_id", newNameID).
                Int("level", level).
                Msg("acquiring neighbours");
        // We will return an unsorted list of level-lists.
        List<List<SkipNodeIdentity>> newTable = new ArrayList<>(numLevels);
        for(int i = 0; i < numLevels; i++) {
            newTable.add(new ArrayList<>());
        }
        // Add the neighbors from the 0-level neighbors.
        nodes.stream()
                .limit(2)
                .flatMap(Collection::stream)
                .filter(x -> !x.equals(LookupTable.EMPTY_NODE))
                .forEach(neighbor -> {
                    int l = SkipNodeIdentity.commonBits(neighbor.getNameID(), newNameID);
                    // Add the neighbor at the max level.
                    newTable.get(l).add(neighbor);
                });
        // Add the owner of this lookup table to the appropriate levels.
        int l = SkipNodeIdentity.commonBits(owner.getNameID(), newNameID);
        newTable.get(l).add(owner);
        lock.readLock().unlock();
        // Return the new lookup table.
        return new TentativeTable(true, -1, newTable);
    }

    @Override
    public void initializeTable(TentativeTable tentativeTable) {
        lock.writeLock().lock();
        // Insert every neighbor at the correct level & direction.
        for(int l = 0; l < tentativeTable.neighbors.size(); l++) {
            List<SkipNodeIdentity> leftList = tentativeTable.neighbors.get(l).stream()
                    .filter(x -> x.getNumID() <= owner.getNumID())
                    .collect(Collectors.toList());
            List<SkipNodeIdentity> rightList = tentativeTable.neighbors.get(l).stream()
                    .filter(x -> x.getNumID() > owner.getNumID())
                    .collect(Collectors.toList());
            for(int j = 0; j <= l; j++) {
                int lIndex = getIndex(direction.LEFT, j);
                int rIndex = getIndex(direction.RIGHT, j);
                nodes.get(lIndex).addAll(leftList);
                nodes.get(rIndex).addAll(rightList);
            }
        }
        // Sort all the entries.
        for(int l = 0; l < numLevels; l++) {
            int lIndex = getIndex(direction.LEFT, l);
            int rIndex = getIndex(direction.RIGHT, l);
            Collections.sort(nodes.get(lIndex));
            Collections.reverse(nodes.get(lIndex));
            Collections.sort(nodes.get(rIndex));
        }
        lock.writeLock().unlock();
    }

    @Override
    public int getNumLevels() {
        return this.numLevels;
    }

    private int getIndex(direction dir, int level) {
        if(level < 0) return Integer.MAX_VALUE;
        if(dir == direction.LEFT) {
            return level*2;
        } else {
            return level*2+1;
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("ConcurrentBackupTable");
        sb.append('\n');
        for(int i=getNumLevels()-1; i>=0; i--){
            List<SkipNodeIdentity> lefts = getLefts(i);
            sb.append("Level:\t");
            sb.append(i);
            sb.append('\n');
            sb.append("Lefts:\t");
            for(int j = lefts.size()-1; j>=0;j--){
                sb.append(lefts.get(j).getNameID());
                sb.append('\t');
            }

            sb.append("Rights:\t");
            List<SkipNodeIdentity> rights = getRights(i);
            for(int j=0; j<rights.size();j++){
                sb.append(rights.get(j).getNameID());
                sb.append('\t');
            }
            sb.append('\n');
        }
        return sb.toString();
    }
}

