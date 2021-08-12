package integration;


/**
 The goal of the mvpTest is to establish a decentralized Skip Graph overlay of 32 nodes and test for full connectivity over each node,
 i.e., each node should be able to query every other node by both name and numerical IDs and get the correct response.
 */

import lookup.LookupTable;
import lookup.LookupTableFactory;
import middlelayer.MiddleLayer;
import model.NameId;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import skipnode.SearchResult;
import skipnode.SkipNode;
import skipnode.SkipNodeIdentity;
import underlay.Underlay;

import java.util.*;
import java.util.stream.Collectors;

import static misc.LocalSkipGraph.prependToLength;

public class mvpTest {
    static int STARTING_PORT = 4444;
    static int NODES = 32;
    static ArrayList<SkipNode> skipNodes;

    void createSkipGraph(){
        List<Underlay> underlays = new ArrayList<>(NODES);
        for(int i = 0; i < NODES; i++) {
            Underlay underlay = Underlay.newDefaultUnderlay();
            underlay.initialize(STARTING_PORT + i);
            underlays.add(underlay);
        }

        String localAddress = underlays.get(0).getAddress();

        int nameIDSize = NameId.computeSize(NODES);
        // Create the numerical IDs.
        List<Integer> numIDs = new ArrayList<>(NODES);
        for(int i = 0; i < NODES; i++) numIDs.add(i);
        // Create the name IDs.
        List<String> nameIDs = numIDs.stream()
                .map(numID -> prependToLength(Integer.toBinaryString(numID), nameIDSize))
                .collect(Collectors.toList());
        // Randomly assign name IDs.
        Collections.shuffle(nameIDs);
        nameIDs.forEach(x -> System.out.print(x + " "));
        System.out.println();
        // Create the identities.
        List<SkipNodeIdentity> identities = new ArrayList<>(NODES);
        for(int i = 0; i < NODES; i++) {
            identities.add(new SkipNodeIdentity(nameIDs.get(i), numIDs.get(i), localAddress, STARTING_PORT + i));
        }
        // Construct the lookup tables.
        List<LookupTable> lookupTables = new ArrayList<>(NODES);
        for(int i = 0; i < NODES; i++) lookupTables.add(LookupTableFactory.createDefaultLookupTable(nameIDSize));

        // Finally, construct the nodes.
        skipNodes = new ArrayList<>(NODES);
        for(int i = 0; i < NODES; i++) {
            SkipNode skipNode = new SkipNode(identities.get(i), lookupTables.get(i));
            skipNodes.add(skipNode);
        }


        // Create the middle layers.
        for(int i = 0; i < NODES; i++) {
            MiddleLayer middleLayer = new MiddleLayer(underlays.get(i), skipNodes.get(i));
            // Assign the middle layer to the underlay & overlay.
            underlays.get(i).setMiddleLayer(middleLayer);
            skipNodes.get(i).setMiddleLayer(middleLayer);
        }

        skipNodes.get(0).insert(null, -1);
    }

    void doInsertions(){
        Thread[] threads = new Thread[NODES-1];
        // Construct the threads.
        for(int i = 1; i <= threads.length; i++) {
            final SkipNode node  = skipNodes.get(i);
            Random rand = new Random();
            final SkipNode introducer = skipNodes.get((int)(Math.random() * i));
            threads[i-1] = new Thread(() -> {
                node.insert(introducer.getIdentity().getAddress(), introducer.getIdentity().getPort());
            });
        }
        // Initiate the insertions.
        for(Thread t : threads) t.start();
        // Wait for the insertions to complete.
        for(Thread t : threads) {
            try {
                t.join();
            } catch (InterruptedException e) {
                System.err.println("Could not join the thread.");
                e.printStackTrace();
            }
        }
    }

    void doSearches(){
        Thread[] searchThreads = new Thread[NODES * NODES];
        for(int i = 0; i < NODES; i++) {
            // Choose the searcher.
            final SkipNode searcher = skipNodes.get(i);
            for(int j = 0; j < NODES; j++) {
                // Choose the target.
                final SkipNode target = skipNodes.get(j);
                searchThreads[i + NODES * j] = new Thread(() -> {
                    // Wait for at most 5 seconds to avoid congestion.
                    try {
                        Thread.sleep((int)(Math.random() * 5000));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    SearchResult res = searcher.searchByNameId(target.getNameId());
                    Assertions.assertEquals(target.getNameId(), res.result.getNameId(), "Source: " + searcher.getNumId() + " Target: " + target.getNameId());
                });
            }
        }
        // Start the search threads.
        for(Thread t : searchThreads) {
            t.start();
        }
        // Complete the threads.
        try {
            for(Thread t : searchThreads) t.join();
        } catch(InterruptedException e) {
            System.err.println("Could not join the thread.");
            e.printStackTrace();
        }
    }

    @Test
    void MVPTEST(){
        createSkipGraph();
        doInsertions();
        doSearches();
    }
}