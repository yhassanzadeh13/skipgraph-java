package integration;


/*


To establish a decentralized (and not local) Skip Graph overlay of 32 nodes and test for full connectivity over each node,
 i.e., each node should be able to query every other node by both name and numerical IDs and get the correct response.

The scenario should involve the first node creating itself through SkipNode constructor, then the rest of the nodes concurrently
create themselves and try inserting in the skip graph using SkipNode.insert method by giving the first node as the introducer.
 **Note: for this we need to override another version of SkipNode constructor that does not require the LookupTable:

public SkipNode(SkipNodeIdentity snID)

We then run tests that each node queries a search for num ID of every other node concurrently and evaluate that the result is correct.
 We then do the same test for search for name ID.

Note that in developing this issue we can borrow a lot of test logics from the already provided concurrentInsertion, searchByNameID,
and searchByNumID. However, we need to be mindful that these tests are using LocalSkipGraph, however, we should have
 separate SkipNodes communicating with each other imitating a decentralized overlay. We can still keep the SkipNodes in
 an array or array list, but must not enforce any centralized behavior.

 */

import lookup.LookupTable;
import lookup.LookupTableFactory;
import middlelayer.MiddleLayer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import skipnode.SearchResult;
import skipnode.SkipNode;
import skipnode.SkipNodeIdentity;
import underlay.Underlay;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static misc.LocalSkipGraph.prependToLength;

public class mvpTest {
    static int STARTING_PORT = 8080;
    static int NODES = 32;

    @Test
    void MVPTEST(){
        List<Underlay> underlays = new ArrayList<>(NODES);
        for(int i = 0; i < NODES; i++) {
            Underlay underlay = Underlay.newDefaultUnderlay();
            underlay.initialize(STARTING_PORT + i);
            underlays.add(underlay);
        }

        String localAddress = underlays.get(0).getAddress();

        int nameIDSize = ((int) (Math.log(NODES)/Math.log(2)));
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
        ArrayList<SkipNode> skipNodes = new ArrayList<>(NODES);
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

        SkipNode initiator = new SkipNode(identities.get(0), nameIDSize);

        Thread[] threads = new Thread[NODES-1];
        System.out.println("testing");

        // Construct the threads.
        for(int i = 1; i <= threads.length; i++) {
            final SkipNode node = skipNodes.get(i);
            threads[i-1] = new Thread(() -> {
                node.insert(initiator.getIdentity().getAddress(), initiator.getIdentity().getPort());
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

        System.out.println("testing insertions 1");

        Thread[] searchThreads = new Thread[NODES * NODES];
        for(int i = 0; i < 1; i++) {
            // Choose the searcher.
            final SkipNode searcher = skipNodes.get(i);
            for(int j = 0; j < 1; j++) {
                // Choose the target.
                final SkipNode target = skipNodes.get(j);
                searchThreads[i + NODES * j] = new Thread(() -> {
                    // Wait for at most 5 seconds to avoid congestion.
                    try {
                        Thread.sleep((int)(Math.random() * 5000));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    SearchResult res = searcher.searchByNameID(target.getNameID());
                    Assertions.assertEquals(target.getNameID(), res.result.getNameID(), "Source: " + searcher.getNumID() + " Target: " + target.getNameID());
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
}
