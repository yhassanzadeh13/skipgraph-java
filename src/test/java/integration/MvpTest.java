package integration;


import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import lookup.ConcurrentLookupTable;
import lookup.LookupTable;
import middlelayer.MiddleLayer;
import model.identifier.MembershipVector;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import underlay.Underlay;
import skipnode.SkipNode;
import skipnode.SkipNodeIdentity;
import unittest.IdentifierFixture;
import unittest.MembershipVectorFixture;
import unittest.MockUnderlay;
import unittest.NetworkHub;

/**
 * The goal of the mvpTest is to establish a decentralized Skip Graph overlay of nodes and test for full connectivity over each node,
 * i.e., each node should be able to query every other node by both identifiers and membership vectors and get the correct response.
 */
public class MvpTest {
  // TODO: this test fails if number of nodes increased, we should fix it.
  private static final int NODES = 5;
  private static final int MembershipVectorSize = MembershipVector.computeSize(NODES);
  private ArrayList<SkipNode> skipNodes;

  /**
   * Creates the skip graph (generates skip nodes), initializes the underlays and middle layers. Inserts the first node.
   */
  private void createSkipGraph() {
    List<Underlay> underlays = new ArrayList<>(NODES);
    NetworkHub networkHub = new NetworkHub();
    for (int i = 0; i < NODES; i++) {
      Underlay underlay = new MockUnderlay(networkHub);
      underlay.initialize(0);
      underlays.add(underlay);
    }

    // identities
    List<SkipNodeIdentity> identities = new ArrayList<>(NODES);

    for (int i = 0; i < NODES; i++) {
      SkipNodeIdentity skid = new SkipNodeIdentity(IdentifierFixture.newIdentifier(),
          MembershipVectorFixture.newMembershipVector(),
          underlays.get(i).getAddress(),
          underlays.get(i).getPort());
      identities.add(skid);
      System.out.println("Identity " + i + ": " + skid);
    }

    // Constructs the lookup tables.
    List<LookupTable> lookupTables = new ArrayList<>(NODES);
    for (int i = 0; i < NODES; i++) lookupTables.add(new ConcurrentLookupTable(MembershipVectorSize, identities.get(i)));

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
    CountDownLatch insertionDone = new CountDownLatch(threads.length);

    // Makes node 0 the introducer of all nodes.
    skipNodes.get(0).insert(null, -1);
    final SkipNode introducer = skipNodes.get(0);

    // Construct the threads.
    for (int i = 0; i < threads.length; i++) {
      final SkipNode node = skipNodes.get(i+1);
      // picks random introducer for a node
      threads[i] = new Thread(() -> {
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
      Thread.currentThread().interrupt(); // Propagate the interruption
      throw new RuntimeException("Thread was interrupted", e);
    }
  }


  /**
   * Performs the searches from every node to every other node by identifier.
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
          SkipNodeIdentity res = searcher.searchByIdentifier(target.getIdentity().getIdentifier());
          try {
            Assertions.assertEquals(
                target.getIdentity().getIdentifier(),
                res.getIdentifier(),
                "Source: " + searcher.getIdentity().getIdentifier() + " Target: " + target.getIdentity().getIdentifier());
          } catch (AssertionError error) {
            System.out.println("Source: " + searcher.getIdentity().getIdentifier() + " Target: " + target.getIdentity().getIdentifier() + " Result: " + res.getIdentifier());
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
   * Create a decentralized skipGraph, insert the nodes concurrently, do searches based on membership vector and identifier.
   */
  @Test
  public void MVP_TEST() {
    createSkipGraph();
    doInsertions();
    doSearches();
  }

  /**
   * Terminates all nodes to free up resources.
   */
  @AfterEach
  public void TearDown(){
    for(SkipNode skipNode: skipNodes){
      Assertions.assertTrue(skipNode.terminate());
    }
  }
}
