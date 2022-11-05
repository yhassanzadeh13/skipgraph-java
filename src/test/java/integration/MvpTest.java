package integration;


import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import lookup.ConcurrentLookupTable;
import lookup.LookupTable;
import middlelayer.MiddleLayer;
import misc.Utils;
import model.identifier.MembershipVector;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import underlay.Underlay;
import skipnode.SearchResult;
import skipnode.SkipNode;
import skipnode.SkipNodeIdentity;
import unittest.IdentifierFixture;
import unittest.MembershipVectorFixture;

/**
 * The goal of the mvpTest is to establish a decentralized Skip Graph overlay of nodes and test for full connectivity over each node,
 * i.e., each node should be able to query every other node by both name and numerical IDs and get the correct response.
 */
public class MvpTest {
  private static final int NODES = 32;
  private static final int NameIdSize = MembershipVector.computeSize(NODES);
  private static ArrayList<SkipNode> skipNodes;

  /**
   * Creates the skip graph (generates skip nodes), initializes the underlays and middle layers. Inserts the first node.
   */
  private void createSkipGraph() {
    List<Underlay> underlays = new ArrayList<>(NODES);

    for (int i = 0; i < NODES; i++) {
      Underlay underlay = Underlay.newDefaultUnderlay();
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
   * Does searches based on nameID and numID concurrently for all of the node pairs.
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
          SearchResult res = searcher.searchByMembershipVector(target.getIdentity().getMembershipVector());
          try {
            Assertions.assertEquals(
                target.getIdentity().getMembershipVector(),
                res.result.getMembershipVector(),
                "Source: " + searcher.getIdentity().getMembershipVector() + " Target: " + target.getIdentity().getMembershipVector());
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
   * Create a decentralized skipGraph, insert the nodes concurrently, do searches based on nameID and numID concurrently.
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
  @AfterAll
  public static void TearDown(){
    for(SkipNode skipNode: skipNodes){
      Assertions.assertTrue(skipNode.terminate());
    }
  }
}
