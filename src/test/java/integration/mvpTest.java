package integration;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import lookup.ConcurrentLookupTable;
import lookup.LookupTable;
import middlelayer.MiddleLayer;
import misc.Utils;
import model.NameId;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import underlay.Underlay;
import static misc.LocalSkipGraph.prependToLength;
import skipnode.SearchResult;
import skipnode.SkipNode;
import skipnode.SkipNodeIdentity;

/**
 * The goal of the mvpTest is to establish a decentralized Skip Graph overlay of nodes and test for full connectivity over each node,
 * i.e., each node should be able to query every other node by both name and numerical IDs and get the correct response.
 */
public class mvpTest {
  static final int STARTING_PORT = 4444;
  static final int NODES = 32;
  static final int NameIdSize = NameId.computeSize(NODES);
  static ArrayList<SkipNode> skipNodes;

  /**
   * Creates the skip graph (generates skip nodes), initializes the underlays and middle layers. Inserts the first node.
   */
  private void createSkipGraph() {
    List<Underlay> underlays = new ArrayList<>(NODES);

    for (int i = 0; i < NODES; i++) {
      Underlay underlay = Underlay.newDefaultUnderlay();
      underlay.initialize(STARTING_PORT + i);
      underlays.add(underlay);
    }


    // numerical and name IDs
    List<Integer> numIDs = new ArrayList<>(NODES);
    for (int i = 0; i < NODES; i++) numIDs.add(i);
    List<String> nameIDs = numIDs.stream()
        .map(numID -> prependToLength(Integer.toBinaryString(numID), NameIdSize))
        .collect(Collectors.toList());
    // Randomly assign name IDs.
    Collections.shuffle(nameIDs);

    // identities
    List<SkipNodeIdentity> identities = new ArrayList<>(NODES);

    for (int i = 0; i < NODES; i++) {
      identities.add(
          new SkipNodeIdentity(nameIDs.get(i),
              numIDs.get(i),
              underlays.get(i).getAddress(),
              underlays.get(i).getPort()));
    }

    // Constructs the lookup tables.
    List<LookupTable> lookupTables = new ArrayList<>(NODES);
    for (int i = 0; i < NODES; i++) lookupTables.add(new ConcurrentLookupTable(NameIdSize, identities.get(i)));

    // Finally, constructs the nodes.
    skipNodes = new ArrayList<>(NODES);
    for (int i = 0; i < NODES; i++) {
      SkipNode skipNode = new SkipNode(identities.get(i), lookupTables.get(i));
      skipNodes.add(skipNode);
    }


    // Create the middlelayers and wires in the underlays to the nodes
    for (int i = 0; i < NODES; i++) {
      MiddleLayer middleLayer = new MiddleLayer(underlays.get(i), skipNodes.get(i));
      // Assign the middle layer to the underlay & overlay.
      underlays.get(i).setMiddleLayer(middleLayer);
      skipNodes.get(i).setMiddleLayer(middleLayer);
    }

    // first node inserts itself
    skipNodes.get(0).insert(null, -1);
  }

  /**
   * Inserts all the skip graph nodes (besides the first one), concurrently
   */
  private void doInsertions() {
    Thread[] threads = new Thread[NODES - 1];
    Random random = new Random();

    // Construct the threads.
    for (int i = 1; i <= threads.length; i++) {
      final SkipNode node = skipNodes.get(i);
      // picks random introducer for a node
      final SkipNode introducer = (SkipNode) Utils.randomIndex(skipNodes, random, i);
      threads[i - 1] = new Thread(() -> {
        node.insert(introducer.getIdentity().getAddress(), introducer.getIdentity().getPort());
      });
    }

    // Initiate the insertions.
    for (Thread t : threads) {
      t.start();
    }
    // Wait for the insertions to complete.
    for (Thread t : threads) {
      try {
        t.join(NODES * 500); // join with timeout
      } catch (InterruptedException e) {
        System.err.println("Could not join the thread.");
        e.printStackTrace();
      }
    }
  }

  /**
   * Does searches based on nameID and numID concurrently for all of the node pairs.
   */
  private void doSearches() {
    AtomicInteger assertionErrorCount = new AtomicInteger();
    Thread[] searchThreads = new Thread[NODES * NODES];
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
      for (Thread t : searchThreads) t.join(NODES * 500);
    } catch (InterruptedException e) {
      System.err.println("Could not join the thread.");
      e.printStackTrace();
    }

    Assertions.assertEquals( 0, assertionErrorCount.get(), "unsuccessful searches results"); // no assertion error should happen in any search thread.
  }

  /**
   * Create a decentralized skipGraph, insert the nodes concurrently, do searches based on nameID and numID concurrently.
   */
  @Test
  public void MVP_TEST() {
    createSkipGraph();
    doInsertions();
    doSearches();
  }
}
