package skipnode;

import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import lookup.LookupTable;
import model.identifier.Identifier;
import model.identifier.MembershipVector;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import unittest.LocalSkipGraph;

/**
 * Contains the skip-node tests.
 */
class SkipNodeTest {
  // total number of Skip Graph nodes involved in the test.
  static final int NODES = 20;
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
        System.out.println("Insertion done " + insertionDone);
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
            System.err.println("Search failed from " + initiator.getIdentity()
                .getMemVec() + " expected: " + target.getIdentity().getMemVec() + " got: " + res.result.getMemVec());
            searchFailed.incrementAndGet();
          }
          searchDone.countDown();
          System.out.println(searchDone);
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

  /**
   * Concurrently inserts all nodes in the graph and checks the correctness of the lookup tables.
   */
  @Test
  void concurrentInsertions() {
    // Insert the first node.
    g.getNodes().get(0).insert(null, -1);
    Thread[] threads = new Thread[NODES - 1];

    CountDownLatch insertionDone = new CountDownLatch(NODES - 1);
    for (int i = 1; i <= threads.length; i++) {
      // Choose an already inserted introducer.
      final int introducerIndex = (int) (Math.random() * i);
      final SkipNode introducer = g.getNodes().get(introducerIndex);
      final SkipNode node = g.getNodes().get(i);
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
      boolean doneOnTime = insertionDone.await(20, TimeUnit.SECONDS);
      Assertions.assertTrue(doneOnTime);
    } catch (InterruptedException e) {
      Assertions.fail(e);
    }

    // Create a map of num ids to their corresponding lookup tables.
    Map<SkipNodeIdentity, LookupTable> idMap = g.getNodes().stream().collect(Collectors.toMap(SkipNode::getIdentity,
        SkipNode::getLookupTable));
    // Create a map of identifiers to their corresponding lookup tables.
    Map<Identifier, LookupTable> tableMap = g.getNodes().stream().map(SkipNode::getIdentity).collect(Collectors.toMap(
        SkipNodeIdentity::getIdentifier,
        idMap::get));

    // Check the correctness & consistency of the tables.
    for (SkipNode n : g.getNodes()) {
      // TODO: replace with streams
      tableCorrectnessCheck(n.getIdentity().getIdentifier(), n.getIdentity().getMemVec(), n.getLookupTable());
      tableConsistencyCheck(tableMap, n);
    }
  }

  /**
   * Sequentially inserts all the nodes in the Skip Graph and checks the correctness of the lookup tables.
   */
  @Test
  void sequentialInsertion() {
    g.insertAllRandomized();
    // Creates a map of identities to their corresponding lookup tables.
    Map<SkipNodeIdentity, LookupTable> idMap = g.getNodes().stream().collect(Collectors.toMap(SkipNode::getIdentity,
        SkipNode::getLookupTable));
    // Creates a map of identifiers to their corresponding lookup tables.
    Map<Identifier, LookupTable> tableMap = g.getNodes().stream().map(SkipNode::getIdentity).collect(Collectors.toMap(
        SkipNodeIdentity::getIdentifier,
        idMap::get));
    // Check the correctness of the tables.
    for (SkipNode n : g.getNodes()) {
      tableCorrectnessCheck(n.getIdentity().getIdentifier(), n.getIdentity().getMemVec(), n.getLookupTable());
      tableConsistencyCheck(tableMap, n);
    }
  }


  /**
   * Inserts all nodes sequentially. Then searches for every node from each
   * node in the skip graph using the identifier.
   */
  @Test
  void sequentialSearchByIdentifier() {
    g.insertAllRandomized();

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
   * Concurrently searches for every node from each node in the skip graph using the identifier.
   * Inserts all nodes using the local skip graph insert method (not the network).
   */
  @Test
  void concurrentSearchByIdentifier() {
    g.insertAllRandomized();

    CountDownLatch searchDone = new CountDownLatch(NODES * NODES);
    AtomicInteger searchFailed = new AtomicInteger(0);
    Thread[] searchThreads = new Thread[NODES * NODES];
    for (int i = 0; i < NODES; i++) {
      final SkipNode initiator = g.getNodes().get(i);
      for (int j = 0; j < NODES; j++) {
        final SkipNode target = g.getNodes().get(j);
        searchThreads[NODES * i + j] = new Thread(() -> {
          SkipNodeIdentity res = initiator.searchByNumId(target.getIdentity().getIdentifier());
          if (!target.getIdentity().getIdentifier().equals(res.getIdentifier())) {
            System.err.println("Search failed from " + initiator.getIdentity()
                .getMemVec() + " expected: " + target.getIdentity().getIdentifier() + " got: " + res.getIdentifier());
            searchFailed.incrementAndGet();
          }
          searchDone.countDown();
          System.out.println(searchDone);
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

  /**
   * Inserts all nodes sequentially. Then searches for every node from each
   * node in the skip graph using membership vector.
   * Inserts all nodes using the local skip graph insert method (not the network).
   */
  @Test
  void sequentialSearchByMembershipVector() {
    g.insertAllRandomized();
    for (int i = 0; i < NODES; i++) {
      SkipNode initiator = g.getNodes().get(i);
      for (int j = 0; j < NODES; j++) {
        SkipNode target = g.getNodes().get(j);
        SearchResult result = initiator.searchByMembershipVector(target.getIdentity().getMemVec());
        if (!result.result.equals(target.getIdentity())) {
          initiator.searchByMembershipVector(target.getIdentity().getMemVec());
        }
        Assertions.assertEquals(target.getIdentity(), result.result);
      }
    }
  }

  // TODO: add concurrent search by membership vector test.

}