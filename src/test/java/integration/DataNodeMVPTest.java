package integration;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import lookup.ConcurrentLookupTable;
import lookup.LookupTable;
import middlelayer.MiddleLayer;
import misc.Utils;
import model.NameId;
import org.junit.jupiter.api.AfterAll;
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


    /**
     * Method borrowed from MVPTest
     */
    void createSkipGraph() {
        underlays = new ArrayList<>(NODES);
        for (int i = 0; i < NODES; i++) {
            Underlay underlay = Underlay.newDefaultUnderlay();
            underlay.initialize(0);
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
        // Create the identities.
        List<SkipNodeIdentity> identities = new ArrayList<>(NODES);
        for (int i = 0; i < NODES; i++) {
            identities.add(new SkipNodeIdentity(nameIDs.get(i), numIDs.get(i), localAddress, underlays.get(i).getPort()));
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
        CountDownLatch insertionDone = new CountDownLatch(DATANODESPERNODE);

        SkipNodeInterface node = skipNodes.get(0);
        for (int i = 0; i < DATANODESPERNODE; i++) {
            SkipNodeIdentity dnID = new SkipNodeIdentity(nameIDs.get(i), numIDs.get(i),
                    node.getIdentity().getAddress(), node.getIdentity().getPort());
            LookupTable lt = new ConcurrentLookupTable(nameIdSize, dnID);
            SkipNode dNode = new SkipNode(dnID, lt);
            tableMap.put(numIDs.get(i), lt);
            threads.add(new Thread(() -> {
                node.insertDataNode(dNode);
                insertionDone.countDown();
            }));
        }
        // Initiate the insertions.
        for (Thread t : threads) {
            t.start();
        }

        try {
            boolean doneOnTime = insertionDone.await(60, TimeUnit.SECONDS);
            Assertions.assertTrue(doneOnTime, "could not perform insertion on time");
        } catch (InterruptedException e) {
            System.err.println("Could not join the thread.");
            e.printStackTrace();
        }
    }

    /**
     * Inserts multiple data nodes to two skip nodes
     */
    void insertMultipleDataNodesToTwoSkipNodes() {
        ArrayList<Thread> threads = new ArrayList<>();
        CountDownLatch insertionDone = new CountDownLatch(DATANODESPERNODE * 2);

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
                    insertionDone.countDown();
                }));
            }
        }
        // Initiate the insertions.
        for (Thread t : threads) {
            t.start();
        }
        // Wait for the insertions to complete.
        try {
            boolean doneOnTime = insertionDone.await(60, TimeUnit.SECONDS);
            Assertions.assertTrue(doneOnTime, "could not perform insertion on time");
        } catch (InterruptedException e) {
            System.err.println("Could not join the thread.");
            e.printStackTrace();
        }
    }

    /**
     * The most complete test
     */
    void insertAllDataNodes() {
        int numDNodes = 0;
        ArrayList<Thread> threads = new ArrayList<>();
        CountDownLatch insertionDone = new CountDownLatch(skipNodes.size() * DATANODESPERNODE);

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
                    insertionDone.countDown();
                }));
            }
        }
        // Initiate the insertions.
        for (Thread t : threads) {
            t.start();
        }
        // Wait for the insertions to complete.
        try {
            boolean doneOnTime = insertionDone.await(60, TimeUnit.SECONDS);
            Assertions.assertTrue(doneOnTime, "could not perform insertion on time");
        } catch (InterruptedException e) {
            System.err.println("Could not join the thread.");
            e.printStackTrace();
        }
    }

    /**
     * Method borrowed from MVPTest
     */
    void insertNodes() {
        Thread[] threads = new Thread[NODES - 1];
        Random random = new Random();
        CountDownLatch insertionDone = new CountDownLatch(threads.length);

        // Construct the threads.
        for (int i = 1; i <= threads.length; i++) {

            final SkipNode node = skipNodes.get(i);
            // picks random introducer for a node
            final SkipNode introducer = (SkipNode) Utils.randomIndex(skipNodes, random, i);
            threads[i - 1] = new Thread(() -> {
                node.insert(introducer.getIdentity().getAddress(), introducer.getIdentity().getPort());
                insertionDone.countDown();
            });
        }

        // Initiate the insertions.
        for (Thread t : threads) {
            t.start();
        }
        // Wait for the insertions to complete.
        try {
            boolean doneOnTime = insertionDone.await(60, TimeUnit.SECONDS);
            Assertions.assertTrue(doneOnTime, "could not perform insertion on time");
        } catch (InterruptedException e) {
            System.err.println("Could not join the thread.");
            e.printStackTrace();
        }
    }

    /**
     * Method borrowed from MVPTest
     */
    private void doSearches() {
        AtomicInteger assertionErrorCount = new AtomicInteger();
        Thread[] searchThreads = new Thread[NODES * NODES];
        CountDownLatch searchDone = new CountDownLatch(searchThreads.length);

        for (int i = 0; i < NODES; i++) {
            // Choose the searcher.
            final SkipNode searcher = skipNodes.get(i);
            for (int j = 0; j < NODES; j++) {
                // Choose the target.
                final SkipNode target = skipNodes.get(j);
                searchThreads[i + NODES * j] = new Thread(() -> {
                    SearchResult res = searcher.searchByNameId(target.getNameId());
                    try {
                        Assertions.assertEquals(target.getNameId(), res.result.getNameId(), "Source: " + searcher.getNumId() + " Target: " + target.getNameId());
                    } catch (AssertionError error) {
                        assertionErrorCount.getAndIncrement();
                    } finally {
                        searchDone.countDown();
                    }
                });
            }
        }

        // Start the search threads.
        for (Thread t : searchThreads) {
            t.start();
        }
        // Complete the threads.
        try {
            boolean doneOnTime = searchDone.await(60, TimeUnit.SECONDS);
            Assertions.assertTrue(doneOnTime, "could not perform searches on time");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Assertions.assertEquals(0, assertionErrorCount.get(), "unsuccessful searches results"); // no assertion error should happen in any search thread.
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
        for (int i=0; i<4; i++) {
            createSkipGraph();
            // Create a map of num ids to their corresponding lookup tables.
            tableMap = skipNodes.stream()
                .collect(Collectors.toMap(SkipNode::getNumId, SkipNode::getLookupTable));
            tableChecks();

            insertNodes();

            createIDs();

            switch (i){
                case 0:
                    insertASingleDataNode();
                    break;
                case 1:
                    insertMultipleDataNodesToASingleSkipNode();
                    break;
                case 2:
                    insertMultipleDataNodesToTwoSkipNodes();
                    break;
                case 3:
                    insertAllDataNodes();
                    break;
            }

            tableChecks();
            doSearches();
            underlays.forEach(Underlay::terminate);
        }
    }

    /**
     * Terminates all nodes to free up resources.
     */
    @AfterAll
    public static void TearDown(){
        for(SkipNode skipNode: skipNodes){
            Assertions.assertTrue(skipNode.terminate());
        }
    }

}
