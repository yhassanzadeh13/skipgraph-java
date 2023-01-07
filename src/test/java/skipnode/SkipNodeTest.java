package skipnode;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import lookup.LookupTable;
import model.identifier.Identifier;
import model.identifier.MembershipVector;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import underlay.Underlay;
import unittest.LocalSkipGraph;

/**
 * Contains the skip-node tests.
 */
class SkipNodeTest {
  // total number of Skip Graph nodes involved in the test.
  static final int NODES = 100;
  private LocalSkipGraph g;

  // Checks the correctness of a lookup table owned by the node with the given identity parameters.
  static void tableCorrectnessCheck(Identifier identifier, MembershipVector mv, LookupTable table) {
    for (int i = 0; i < table.getNumLevels(); i++) {
      SkipNodeIdentity left = table.getLeft(i);
      SkipNodeIdentity right = table.getRight(i);

      if (!left.equals(LookupTable.EMPTY_NODE)) {
        Assertions.assertTrue(left.getIdentifier().isLessThan(identifier));
        Assertions.assertTrue(left.getMemVec().commonPrefix(mv) >= i);
      }

      if (!right.equals(LookupTable.EMPTY_NODE)) {
        Assertions.assertTrue(right.getIdentifier().isGreaterThan(identifier));
        Assertions.assertTrue(right.getMemVec().commonPrefix(mv) >= i);
      }
    }
  }

  // Checks the consistency of a lookup table. In other words, we assert that if x is a neighbor of y at level l,
  // then y is a neighbor of x at level l (in opposite directions).
  static void tableConsistencyCheck(Map<Identifier, LookupTable> tableMap, SkipNode node) {
    LookupTable table = node.getLookupTable();
    for (int i = 0; i < table.getNumLevels(); i++) {
      SkipNodeIdentity left = table.getLeft(i);
      SkipNodeIdentity right = table.getRight(i);

      if (!left.equals(LookupTable.EMPTY_NODE)) {
        LookupTable neighborMap = tableMap.get(left.getIdentifier());
        Assertions.assertTrue(neighborMap.isRightNeighbor(node.getIdentity(), i));
      }

      if (!right.equals(LookupTable.EMPTY_NODE)) {
        LookupTable neighborMap = tableMap.get(right.getIdentifier());
        Assertions.assertTrue(neighborMap.isLeftNeighbor(node.getIdentity(), i));
      }
    }
  }

//   In this test I call the increment a lot of times through different threads
//   This tests whether all messages are in face received or not
//   @Test
//    void concurrentIncrements() {
//        // First, construct the underlays.
//        List<Underlay> underlays = new ArrayList<>(NODES);
//        for(int i = 0; i < NODES; i++) {
//            Underlay underlay = Underlay.newDefaultUnderlay();
//            underlay.initialize(STARTING_PORT + i);
//            underlays.add(underlay);
//        }
//        // Then, construct the local skip graph without manually constructing the lookup tables.
//        LocalSkipGraph g = new LocalSkipGraph(NODES, underlays.get(0).getAddress(), STARTING_PORT, false);
//        // Create the middle layers.
//        for(int i = 0; i < NODES; i++) {
//            MiddleLayer middleLayer = new MiddleLayer(underlays.get(i), g.getNodes().get(i));
//            // Assign the middle layer to the underlay & overlay.
//            underlays.get(i).setMiddleLayer(middleLayer);
//            g.getNodes().get(i).setMiddleLayer(middleLayer);
//        }
////        // We expect the lookup tables to converge to a correct state after SEARCH_THRESHOLD many searches.
////        for(int k = 0; k < SEARCH_THRESHOLD; k++) {
////            final SkipNode initiator = g.getNodes().get((int)(Math.random() * NODES));
////            final SkipNode target = g.getNodes().get((int)(Math.random() * NODES));
////            initiator.searchByNameID(target.getNameID());
////        }
//        // Construct the search threads.
//        Thread[] searchThreads = new Thread[SEARCH_THREADS];
//        final SkipNode target = g.getNodes().get((int)(Math.random() * NODES));
//        for(int i = 0; i < searchThreads.length; i++) {
//            // Choose two random nodes.
//            final SkipNode initiator = g.getNodes().get((int)(Math.random() * NODES));
//            searchThreads[i] = new Thread(() -> {
////                SearchResult res = initiator.searchByNameID(target.getNameID());
//                initiator.increment(target.getIdentity(), 0);
//                initiator.increment(target.getIdentity(), 0);
////                Assertions.assertEquals(target.getNameID(), res.result.getNameID());
//            });
//        }
//        // Start the search threads.
//        for(Thread t : searchThreads) t.start();
//        // Complete the threads.
//        try {
//            for(Thread t : searchThreads) t.join();
//        } catch(InterruptedException e) {
//            System.err.println("Could not join the thread.");
//            e.printStackTrace();
//        }
//        int sum = 0;
//        // One should be 2 * NUMTHREADS, rest should be 0
//        for (SkipNode node : g.getNodes()){
//            System.out.println(node.i);
//            sum+=node.i.get();
//        }
//        // This should be 2 * NUMTHREADS
//        System.out.println(sum);
//    }

  @BeforeEach
  public void setup() {
    g = new LocalSkipGraph(NODES, false);
  }

  @AfterEach
  public void teardown() {
    g.terminate();
  }

  @Test
  void concurrentInsertionsAndSearches() {
    // Insert the first node.
    g.getNodes().get(0).insert(null, -1);

    CountDownLatch insertionDone = new CountDownLatch(NODES - 1);
    // insertion thread for all other nodes.
    Thread[] insertionThreads = new Thread[NODES - 1];
    for (int i = 1; i <= insertionThreads.length; i++) {
      // choose the previous node as the introducer.
      final SkipNode introducer = g.getNodes().get(i - 1);
      final SkipNode node = g.getNodes().get(i);
      insertionThreads[i - 1] = new Thread(() -> {
        node.insert(introducer.getIdentity().getAddress(), introducer.getIdentity().getPort());
        insertionDone.countDown();
        System.out.println("Insertion done " + insertionDone.toString());
      });
    }


    for (Thread t : insertionThreads) {
      t.start();
    }

    try {
      boolean doneOnTime = insertionDone.await(200, TimeUnit.SECONDS);
      Assertions.assertTrue(doneOnTime);
    } catch (InterruptedException e) {
      Assertions.fail(e);
    }

    // check the correctness and consistency of the lookup tables.
    Map<Identifier, LookupTable> tableMap = g.identifierLookupTableMap();
    for (SkipNode n : g.getNodes()) {
      tableCorrectnessCheck(n.getIdentity().getIdentifier(), n.getIdentity().getMemVec(), n.getLookupTable());
      tableConsistencyCheck(tableMap, n);
    }

    // Construct the search threads we will perform searches from each node to every node.
    CountDownLatch searchDone = new CountDownLatch(NODES * NODES);
    AtomicInteger searchFailed = new AtomicInteger(0);
    Thread[] searchThreads = new Thread[NODES * NODES];
    for (int i = 0; i < NODES; i++) {
      final SkipNode initiator = g.getNodes().get(i);
      for (int j = 0; j < NODES; j++) {
        final SkipNode target = g.getNodes().get(j);
        searchThreads[i + NODES * j] = new Thread(() -> {
          SearchResult res = initiator.searchByMembershipVector(target.getIdentity().getMemVec());
          if (!target.getIdentity().getMemVec().equals(res.result.getMemVec())) {
            System.err.println("Search failed from " + initiator.getIdentity().getMemVec() + " expected: " + target.getIdentity().getMemVec() + " got: " + res.result.getMemVec());
            searchFailed.incrementAndGet();
          }
          searchDone.countDown();
          System.out.println(searchDone.toString());
        });
      }
    }
    // Start the search threads.
    for (Thread t : searchThreads) {
      t.start();
    }
    // Complete the threads.
    try {
      boolean doneOnTime = searchDone.await(20, TimeUnit.SECONDS);
      Assertions.assertTrue(doneOnTime);
    } catch (InterruptedException e) {
      Assertions.fail(e);
    }
    Assertions.assertEquals(0, searchFailed.get(), "some searches failed");
  }

//  @Test
//  void concurrentInsertions() {
//    // First, construct the underlays.
//    List<Underlay> underlays = new ArrayList<>(NODES);
//    for (int i = 0; i < NODES; i++) {
//      Underlay underlay = Underlay.newDefaultUnderlay();
//      underlay.initialize(STARTING_PORT + i + 2 * NODES);
//      underlays.add(underlay);
//    }
//    // Then, construct the local skip graph without manually constructing the lookup tables.
//    LocalSkipGraph g = new LocalSkipGraph(NODES, underlays.get(0).getAddress(),
//        STARTING_PORT + NODES * 2, false);
//    // Create the middle layers.
//    for (int i = 0; i < NODES; i++) {
//      MiddleLayer middleLayer = new MiddleLayer(underlays.get(i), g.getNodes().get(i));
//      // Assign the middle layer to the underlay & overlay.
//      underlays.get(i).setMiddleLayer(middleLayer);
//      g.getNodes().get(i).setMiddleLayer(middleLayer);
//    }
//    // Insert the first node.
//    g.getNodes().get(0).insert(null, -1);
//    Thread[] threads = new Thread[NODES - 1];
//    // Construct the threads.
//    for (int i = 1; i <= threads.length; i++) {
//      // Choose an already inserted introducer.
//      final SkipNode introducer = g.getNodes().get((int) (Math.random() * i));
//      final SkipNode node = g.getNodes().get(i);
//      threads[i - 1] = new Thread(() -> {
//        node.insert(introducer.getIdentity().getAddress(), introducer.getIdentity().getPort());
//      });
//    }
//    // Initiate the insertions.
//    for (Thread t : threads) {
//      t.start();
//    }
//    // Wait for the insertions to complete.
//    for (Thread t : threads) {
//      try {
//        t.join();
//      } catch (InterruptedException e) {
//        System.err.println("Could not join the thread.");
//        e.printStackTrace();
//      }
//    }
//    // Create a map of num ids to their corresponding lookup tables.
//    Map<SkipNodeIdentity, LookupTable> idMap = g.getNodes().stream()
//        .collect(Collectors.toMap(SkipNode::getIdentity, SkipNode::getLookupTable));
//    // Create a map of identifiers to their corresponding lookup tables.
//    Map<Identifier, LookupTable> tableMap = g.getNodes().stream().map(SkipNode::getIdentity)
//        .collect(Collectors.toMap(SkipNodeIdentity::getIdentifier, idMap::get));
//
//    // Check the correctness & consistency of the tables.
//    for (SkipNode n : g.getNodes()) {
//      // TODO: replace with streams
//      tableCorrectnessCheck(n.getIdentity().getIdentifier(), n.getIdentity().getMembershipVector(), n.getLookupTable());
//      tableConsistencyCheck(tableMap, n);
//    }
//  }

//  @Test
//  void insert() {
//    // First, construct the underlays.
//    List<Underlay> underlays = new ArrayList<>(NODES);
//    for (int i = 0; i < NODES; i++) {
//      Underlay underlay = Underlay.newDefaultUnderlay();
//      underlay.initialize(STARTING_PORT + i + 3 * NODES);
//      underlays.add(underlay);
//    }
//    // Then, construct the local skip graph without manually constructing the lookup tables.
//    LocalSkipGraph g = new LocalSkipGraph(NODES, underlays.get(0).getAddress(),
//        STARTING_PORT + NODES * 3, false);
//    // Create the middle layers.
//    for (int i = 0; i < NODES; i++) {
//      MiddleLayer middleLayer = new MiddleLayer(underlays.get(i), g.getNodes().get(i));
//      // Assign the middle layer to the underlay & overlay.
//      underlays.get(i).setMiddleLayer(middleLayer);
//      g.getNodes().get(i).setMiddleLayer(middleLayer);
//    }
//    // Now, insert every node in a randomized order.
//    g.insertAllRandomized();
//    // Create a map of num ids to their corresponding lookup tables.
//    Map<SkipNodeIdentity, LookupTable> idMap = g.getNodes().stream()
//        .collect(Collectors.toMap(SkipNode::getIdentity, SkipNode::getLookupTable));
//    Map<Identifier, LookupTable> tableMap = g.getNodes().stream().map(SkipNode::getIdentity)
//        .collect(Collectors.toMap(SkipNodeIdentity::getIdentifier, idMap::get));
//    // Check the correctness of the tables.
//    for (SkipNode n : g.getNodes()) {
//      tableCorrectnessCheck(n.getIdentity().getIdentifier(), n.getIdentity().getMembershipVector(), n.getLookupTable());
//      tableConsistencyCheck(tableMap, n);
//    }
//    underlays.forEach(Underlay::terminate);
//  }



  /**
   * Inserts all nodes sequentially. Then searches for every node from each
   * node in the skip graph using the identifier.
   */
  @Test
  void testSearchByIdentifierSequential() {
    g.insertAll();

    for (int i = 0; i < NODES; i++) {
      SkipNode initiator = g.getNodes().get(i);
      for (int j = 0; j < NODES; j++) {
        SkipNode target = g.getNodes().get(j);
        SkipNodeIdentity result = initiator.searchByNumId(target.getIdentity().getIdentifier());
        Assertions.assertEquals(target.getIdentity(), result);
      }
    }
  }

  /**
   * Inserts all nodes sequentially. Then searches for every node from each
   * node in the skip graph using membership vector.
   */
  @Test
  void testSearchByMembershipVectorSequential() {
    System.out.println("Inserting sequentially.");
    g.insertAll();
    System.out.println("Insertion complete.");
    for (int i = 0; i < NODES; i++) {
      SkipNode initiator = g.getNodes().get(i);
      for (int j = 0; j < NODES; j++) {
        SkipNode target = g.getNodes().get(j);
        SearchResult result = initiator.searchByMembershipVector(target.getIdentity().getMemVec());
        if (!result.result.equals(target.getIdentity())) {
          initiator.searchByMembershipVector(target.getIdentity().getMemVec());
        }
        Assertions.assertEquals(target.getIdentity(), result.result);
        System.out.println("done with " + i + " " + j);
      }
    }
  }
}