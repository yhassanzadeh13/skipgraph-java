package skipnode;

import middlelayer.MiddleLayer;
import misc.LocalSkipGraph;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import underlay.Underlay;
import underlay.UnderlayTest;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Contains the skip-node tests.
 */
class SkipNodeTest {

    static int STARTING_PORT = 8080;
    static int NODES = 16;

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
        LocalSkipGraph g = new LocalSkipGraph(NODES, underlays.get(0).getAddress(), STARTING_PORT);
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
                SkipNodeIdentity result = initiator.searchByNameID(target.getNameID());
                Assertions.assertEquals(target.getIdentity(), result);
            }
        }

    }
}