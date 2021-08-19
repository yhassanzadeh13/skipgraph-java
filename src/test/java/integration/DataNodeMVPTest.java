package integration;

import lookup.ConcurrentLookupTable;
import lookup.LookupTable;
import middlelayer.MiddleLayer;
import model.NameId;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import skipnode.SearchResult;
import skipnode.SkipNode;
import skipnode.SkipNodeIdentity;
import skipnode.SkipNodeInterface;
import underlay.Underlay;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static misc.LocalSkipGraph.prependToLength;
import static skipnode.SkipNodeTest.tableConsistencyCheck;
import static skipnode.SkipNodeTest.tableCorrectnessCheck;

public class DataNodeMVPTest {
    static int STARTING_PORT = 8000;
    static int NODES = 8;
    static int DATANODESPERNODE = 3;
    static ArrayList<SkipNode> skipNodes;
    List<Underlay> underlays;
    int nameIdSize;
    Map<Integer, LookupTable> tableMap;
    List<Integer> numIDs;
    List<String> nameIDs;
    List<Consumer> testMethodList = new ArrayList<>();


    /**
     * Method borrowed from MVPTest
     */
    void createSkipGraph() {
        underlays = new ArrayList<>(NODES);
        for (int i = 0; i < NODES; i++) {
            Underlay underlay = Underlay.newDefaultUnderlay();
            underlay.initialize(STARTING_PORT + i);
            underlays.add(underlay);
        }

        String localAddress = underlays.get(0).getAddress();

        nameIdSize = NameId.computeSize(NODES);
        // Create the numerical IDs.
        List<Integer> numIDs = new ArrayList<>(NODES);
        for (int i = 0; i < NODES; i++) numIDs.add(i);
        // Create the name IDs.
        List<String> nameIDs = numIDs.stream()
                .map(numID -> prependToLength(Integer.toBinaryString(numID), nameIdSize))
                .collect(Collectors.toList());
        // Randomly assign name IDs.
        Collections.shuffle(nameIDs);
        nameIDs.forEach(x -> System.out.print(x + " "));
        System.out.println();
        // Create the identities.
        List<SkipNodeIdentity> identities = new ArrayList<>(NODES);
        for (int i = 0; i < NODES; i++) {
            identities.add(new SkipNodeIdentity(nameIDs.get(i), numIDs.get(i), localAddress, STARTING_PORT + i));
        }
        // Construct the lookup tables.
        List<LookupTable> lookupTables = new ArrayList<>(NODES);
        for (int i = 0; i < NODES; i++) lookupTables.add(new ConcurrentLookupTable(nameIdSize, identities.get(i)));

        // Finally, construct the nodes.
        skipNodes = new ArrayList<>(NODES);
        for (int i = 0; i < NODES; i++) {
            SkipNode skipNode = new SkipNode(identities.get(i), lookupTables.get(i));
            skipNodes.add(skipNode);
        }


        // Create the middle layers.
        for (int i = 0; i < NODES; i++) {
            MiddleLayer middleLayer = new MiddleLayer(underlays.get(i), skipNodes.get(i));
            // Assign the middle layer to the underlay & overlay.
            underlays.get(i).setMiddleLayer(middleLayer);
            skipNodes.get(i).setMiddleLayer(middleLayer);
        }

        skipNodes.get(0).insert(null, -1);
    }


    /**
     * Just inserts a single data node
     */
    void insertASingleDataNode() {
        SkipNodeInterface node = skipNodes.get(0);
        SkipNodeIdentity dnID = new SkipNodeIdentity(nameIDs.get(0), numIDs.get(0),
                node.getIdentity().getAddress(), node.getIdentity().getPort());
        LookupTable lt = new ConcurrentLookupTable(nameIdSize, dnID);
        SkipNode dNode = new SkipNode(dnID, lt);
        tableMap.put(numIDs.get(0), lt);
        node.insertDataNode(dNode);
    }

    /**
     * Inserts multiple data nodes to a single skip node
     */
    void insertMultipleDataNodesToASingleSkipNode() {
        ArrayList<Thread> threads = new ArrayList<>();

        SkipNodeInterface node = skipNodes.get(0);
        for (int i = 0; i < DATANODESPERNODE; i++) {
            SkipNodeIdentity dnID = new SkipNodeIdentity(nameIDs.get(i), numIDs.get(i),
                    node.getIdentity().getAddress(), node.getIdentity().getPort());
            LookupTable lt = new ConcurrentLookupTable(nameIdSize, dnID);
            SkipNode dNode = new SkipNode(dnID, lt);
            tableMap.put(numIDs.get(i), lt);
            threads.add(new Thread(() -> {
                node.insertDataNode(dNode);
            }));
        }
        // Initiate the insertions.
        for (Thread t : threads) {
            t.start();
        }
        // Wait for the insertions to complete.
        for (Thread t : threads) {
            try {
                t.join();
            } catch (InterruptedException e) {
                System.err.println("Could not join the thread.");
                e.printStackTrace();
            }
        }
    }

    /**
     * Inserts multiple data nodes to two skip nodes
     */
    void insertMultipleDataNodesToTwoSkipNodes() {
        ArrayList<Thread> threads = new ArrayList<>();
        int numDNodes = 0;
        for (int j = 0; j < 2; j++) {
            for (int i = 0; i < DATANODESPERNODE; i++) {
                SkipNodeInterface node = skipNodes.get(j);
                SkipNodeIdentity dnID = new SkipNodeIdentity(nameIDs.get(numDNodes), numIDs.get(numDNodes),
                        node.getIdentity().getAddress(), node.getIdentity().getPort());
                LookupTable lt = new ConcurrentLookupTable(nameIdSize, dnID);
                SkipNode dNode = new SkipNode(dnID, lt);
                tableMap.put(numIDs.get(numDNodes), lt);
                numDNodes++;
                threads.add(new Thread(() -> {
                    node.insertDataNode(dNode);
                }));
            }
        }
        // Initiate the insertions.
        for (Thread t : threads) {
            t.start();
        }
        // Wait for the insertions to complete.
        for (Thread t : threads) {
            try {
                t.join();
            } catch (InterruptedException e) {
                System.err.println("Could not join the thread.");
                e.printStackTrace();
            }
        }
    }

    /**
     * The most complete test
     */
    void insertAllDataNodes() {
        int numDNodes = 0;
        ArrayList<Thread> threads = new ArrayList<>();
        // Construct the threads.
        for (int j = 0; j < skipNodes.size(); j++) {
            for (int i = 0; i < DATANODESPERNODE; i++) {
                SkipNodeInterface node = skipNodes.get(j);
                SkipNodeIdentity dnID = new SkipNodeIdentity(nameIDs.get(numDNodes), numIDs.get(numDNodes),
                        node.getIdentity().getAddress(), node.getIdentity().getPort());
                LookupTable lt = new ConcurrentLookupTable(nameIdSize, dnID);
                SkipNode dNode = new SkipNode(dnID, lt);
                tableMap.put(numIDs.get(numDNodes), lt);
                numDNodes++;
                threads.add(new Thread(() -> {
                    node.insertDataNode(dNode);
                }));
            }
        }
        // Initiate the insertions.
        for (Thread t : threads) {
            t.start();
        }
        // Wait for the insertions to complete.
        for (Thread t : threads) {
            try {
                t.join();
            } catch (InterruptedException e) {
                System.err.println("Could not join the thread.");
                e.printStackTrace();
            }
        }
    }

    /**
     * Method borrowed from MVPTest
     */
    void insertNodes() {
        Thread[] threads = new Thread[NODES - 1];
        // Construct the threads.
        for (int i = 1; i <= threads.length; i++) {
            final SkipNode node = skipNodes.get(i);
            Random rand = new Random();
            final SkipNode introducer = skipNodes.get((int) (Math.random() * i));
            threads[i - 1] = new Thread(() -> {
                node.insert(introducer.getIdentity().getAddress(), introducer.getIdentity().getPort());
            });
        }
        // Initiate the insertions.
        for (Thread t : threads) t.start();
        // Wait for the insertions to complete.
        for (Thread t : threads) {
            try {
                t.join();
            } catch (InterruptedException e) {
                System.err.println("Could not join the thread.");
                e.printStackTrace();
            }
        }
    }

    /**
     * Method borrowed from MVPTest
     */
    void doSearches() {
        Thread[] searchThreads = new Thread[NODES * NODES];
        for (int i = 0; i < NODES; i++) {
            // Choose the searcher.
            final SkipNode searcher = skipNodes.get(i);
            for (int j = 0; j < NODES; j++) {
                // Choose the target.
                final SkipNode target = skipNodes.get(j);
                searchThreads[i + NODES * j] = new Thread(() -> {
                    // Wait for at most 5 seconds to avoid congestion.
                    try {
                        Thread.sleep((int) (Math.random() * 5000));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    SearchResult res = searcher.searchByNameId(target.getNameId());
                    Assertions.assertEquals(target.getNameId(), res.result.getNameId(), "Source: " + searcher.getNumId() + " Target: " + target.getNameId());
                });
            }
        }
        // Start the search threads.
        for (Thread t : searchThreads) {
            t.start();
        }
        // Complete the threads.
        try {
            for (Thread t : searchThreads) t.join();
        } catch (InterruptedException e) {
            System.err.println("Could not join the thread.");
            e.printStackTrace();
        }
    }

    /**
     * Check the correctness and consistencies of the tables.
     */
    void tableChecks(){
        for (SkipNode n : skipNodes) {
            tableCorrectnessCheck(n.getNumId(), n.getNameId(), n.getLookupTable());
            tableConsistencyCheck(tableMap, n);
        }
    }

    /**
     * Helper to create num and nameIDs for dataNodes.
     */
    void createIDs(){
        // Create numIDs
        numIDs = new ArrayList<>(NODES * DATANODESPERNODE);
        for (int i = NODES; i < NODES * (DATANODESPERNODE + 1); i++) {
            numIDs.add(i);
        }
        // Create the name IDs.
        nameIDs = numIDs.stream()
                .map(numID -> prependToLength(Integer.toBinaryString(numID), nameIdSize))
                .collect(Collectors.toList());
    }

    /**
     * Test inspired by DataNodeTest, with the following differences:
     *
     *     Instead of a LocalSkipGraph we use an actual skip graph similar to mvp test.
     *     We insert data nodes in the Skip Graph (concurrently each insertion in a separate thread).
     *     Instead of just checking for consistency, we search for inserted data nodes concurrently (each search in a thread), and see if the result is correct.
     */
    @Test
    public void dataNodeMVPTest() {
        createSkipGraph();
        // Create a map of num ids to their corresponding lookup tables.
        tableMap = skipNodes.stream()
                .collect(Collectors.toMap(SkipNode::getNumId, SkipNode::getLookupTable));
        tableChecks();

        insertNodes();

        createIDs();

        // we can test different combinations by changing the line below, using the methods above
        insertAllDataNodes();

        tableChecks();
        doSearches();
        underlays.forEach(Underlay::terminate);
    }


}
