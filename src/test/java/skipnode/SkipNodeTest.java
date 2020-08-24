package skipnode;

import lookup.LookupTable;
import middlelayer.MiddleLayer;
import misc.LocalSkipGraph;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import underlay.Underlay;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Contains the skip-node tests.
 */
class SkipNodeTest {

    static int STARTING_PORT = 8080;
    static int NODES = 16;

    @Test
    void insert() {
        // First, construct the underlays.
        List<Underlay> underlays = new ArrayList<>(NODES);
        for(int i = 0; i < NODES; i++) {
            Underlay underlay = Underlay.newDefaultUnderlay();
            underlay.initialize(STARTING_PORT + i);
            underlays.add(underlay);
        }
        // Then, construct the local skip graph without manually constructing the lookup tables.
        LocalSkipGraph g = new LocalSkipGraph(NODES, underlays.get(0).getAddress(), STARTING_PORT, false);
        // Create the middle layers.
        for(int i = 0; i < NODES; i++) {
            MiddleLayer middleLayer = new MiddleLayer(underlays.get(i), g.getNodes().get(i));
            // Assign the middle layer to the underlay & overlay.
            underlays.get(i).setMiddleLayer(middleLayer);
            g.getNodes().get(i).setMiddleLayer(middleLayer);
        }
        // Now, insert every node in a randomized order.
        g.insertAllRandomized();
        // Create a map of num ids to their corresponding lookup tables.
        Map<Integer, LookupTable> tableMap = g.getNodes().stream()
                .collect(Collectors.toMap(SkipNode::getNumID, SkipNode::getLookupTable));
        // Check the correctness of the tables.
        for(SkipNode n : g.getNodes()) {
            tableCorrectnessCheck(n.getNumID(), n.getNameID(), n.getLookupTable());
            tableConsistencyCheck(tableMap, n);
        }
        underlays.forEach(Underlay::terminate);
    }

    @Test
    void searchByNameID() {
        // First, construct the underlays.
        List<Underlay> underlays = new ArrayList<>(NODES);
        for(int i = 0; i < NODES; i++) {
            Underlay underlay = Underlay.newDefaultUnderlay();
            underlay.initialize(STARTING_PORT + i);
            underlays.add(underlay);
        }
        // Then, construct the local skip graph.
        LocalSkipGraph g = new LocalSkipGraph(NODES, underlays.get(0).getAddress(), STARTING_PORT, false);
        // Create the middle layers.
        for(int i = 0; i < NODES; i++) {
            MiddleLayer middleLayer = new MiddleLayer(underlays.get(i), g.getNodes().get(i));
            // Assign the middle layer to the underlay & overlay.
            underlays.get(i).setMiddleLayer(middleLayer);
            g.getNodes().get(i).setMiddleLayer(middleLayer);
        }
        // Insert all the nodes in a randomized fashion.
        g.insertAllRandomized();
        // We will now perform name ID searches for every node from each node in the skip graph.
        for(int i = 0; i < NODES; i++) {
            SkipNode initiator = g.getNodes().get(i);
            for(int j = 0; j < NODES; j++) {
                SkipNode target = g.getNodes().get(j);
                SkipNodeIdentity result = initiator.searchByNameID(target.getNameID());
                Assertions.assertEquals(target.getIdentity(), result);
            }
        }
        underlays.forEach(Underlay::terminate);
    }

    @Test
    void searchByNumID() {
        // First, construct the underlays.
        List<Underlay> underlays = new ArrayList<>(NODES);
        for(int i = 0; i < NODES; i++) {
            Underlay underlay = Underlay.newDefaultUnderlay();
            underlay.initialize(STARTING_PORT - NODES + i);
            underlays.add(underlay);
        }
        // Then, construct the local skip graph.
        LocalSkipGraph g = new LocalSkipGraph(NODES, underlays.get(0).getAddress(), STARTING_PORT- NODES, true);
        // Create the middle layers.
        for(int i = 0; i < NODES; i++) {
            MiddleLayer middleLayer = new MiddleLayer(underlays.get(i), g.getNodes().get(i));
            // Assign the middle layer to the underlay & overlay.
            underlays.get(i).setMiddleLayer(middleLayer);
            g.getNodes().get(i).setMiddleLayer(middleLayer);
        }

        // We will now perform name ID searches for every node from each node in the skip graph.
        for(int i = 0; i < NODES; i++) {
            SkipNode initiator = g.getNodes().get(i);
            for(int j = 0; j < NODES; j++) {
                SkipNode target = g.getNodes().get(j);
                SkipNodeIdentity result = initiator.searchByNumID(target.getNumID());
                Assertions.assertEquals(target.getIdentity(), result);
            }
        }
        underlays.forEach(Underlay::terminate);
    }

    // Checks the correctness of a lookup table owned by the node with the given identity parameters.
    static void tableCorrectnessCheck(int numID, String nameID, LookupTable table) {
        for(int i = 0; i < table.getNumLevels(); i++) {
            List<SkipNodeIdentity> lefts = table.getLefts(i);
            List<SkipNodeIdentity> rights = table.getRights(i);
            for(SkipNodeIdentity l : lefts) {
                Assertions.assertTrue(l.getNumID() < numID);
                Assertions.assertTrue(SkipNodeIdentity.commonBits(l.getNameID(), nameID) >= i);
            }
            for(SkipNodeIdentity r : rights) {
                Assertions.assertTrue(r.getNumID() > numID);
                Assertions.assertTrue(SkipNodeIdentity.commonBits(r.getNameID(), nameID) >= i);
            }
        }
    }

    // Checks the consistency of a lookup table. In other words, we assert that if x is a neighbor of y at level l,
    // then y is a neighbor of x at level l (in opposite directions).
    static void tableConsistencyCheck(Map<Integer, LookupTable> tableMap, SkipNode node) {
        LookupTable table = node.getLookupTable();
        for(int i = 0; i < table.getNumLevels(); i++) {
            List<SkipNodeIdentity> lefts = table.getLefts(i);
            List<SkipNodeIdentity> rights = table.getRights(i);
            // Check whether the neighbors agree on the neighborship relationships.
            for(SkipNodeIdentity l : lefts) {
                LookupTable neighborMap = tableMap.get(l.getNumID());
                Assertions.assertTrue(neighborMap.isRightNeighbor(node.getIdentity(), i));
            }
            for(SkipNodeIdentity r : rights) {
                LookupTable neighborMap = tableMap.get(r.getNumID());
                Assertions.assertTrue(neighborMap.isLeftNeighbor(node.getIdentity(), i));
            }
        }
    }
}