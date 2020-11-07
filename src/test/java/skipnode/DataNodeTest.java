package skipnode;

import lookup.LookupTable;
import lookup.LookupTableFactory;
import middlelayer.MiddleLayer;
import misc.LocalSkipGraph;
import org.junit.jupiter.api.Test;
import underlay.Underlay;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static misc.LocalSkipGraph.prependToLength;
import static skipnode.SkipNodeTest.tableConsistencyCheck;
import static skipnode.SkipNodeTest.tableCorrectnessCheck;

public class DataNodeTest {
    static int STARTING_PORT = 8000;
    static int NODES = 8;
    static int DATANODESPERNODE = 3;

    @Test
    void testDataNodes(){
        // First, construct the main underlays.
        List<Underlay> underlays = new ArrayList<>(NODES);
        for(int i = 0; i < NODES; i++) {
            Underlay underlay = Underlay.newDefaultUnderlay();
            underlay.initialize(STARTING_PORT + i);
            underlays.add(underlay);
        }
        // Then, construct the local skip graph without manually constructing the lookup tables.
        int nameIDSize = ((int) (Math.log(NODES*(DATANODESPERNODE+1))/Math.log(2)));
        LocalSkipGraph g = new LocalSkipGraph(NODES, underlays.get(0).getAddress(), STARTING_PORT, false, nameIDSize);
        // Create the middle layers.
        for(int i = 0; i < NODES; i++) {
            MiddleLayer middleLayer = new MiddleLayer(underlays.get(i), g.getNodes().get(i));
            // Assign the middle layer to the underlay & overlay.
            underlays.get(i).setMiddleLayer(middleLayer);
            g.getNodes().get(i).setMiddleLayer(middleLayer);
        }
        // Now, insert every node in a randomized order.
        g.insertAll();
        // Create a map of num ids to their corresponding lookup tables.
        Map<Integer, LookupTable> tableMap = g.getNodes().stream()
                .collect(Collectors.toMap(SkipNode::getNumID, SkipNode::getLookupTable));
        // Check the correctness of the tables.
        for(SkipNode n : g.getNodes()) {
            tableCorrectnessCheck(n.getNumID(), n.getNameID(), n.getLookupTable());
            tableConsistencyCheck(tableMap, n);
        }

        // Create datanodes
        List<Integer> numIDs = new ArrayList<>(NODES*DATANODESPERNODE);
        for(int i = NODES; i < NODES*(DATANODESPERNODE+1); i++) numIDs.add(i);
        // Create the name IDs.
        List<String> nameIDs = numIDs.stream()
                .map(numID -> prependToLength(Integer.toBinaryString(numID), nameIDSize))
                .collect(Collectors.toList());
        int numDNodes = 0;

        for(SkipNodeInterface node : g.getNodes()){
            for(int i=0;i<DATANODESPERNODE;i++){
                LookupTable lt = LookupTableFactory.createDefaultLookupTable(nameIDSize);
                SkipNodeIdentity dnID = new SkipNodeIdentity(nameIDs.get(numDNodes), numIDs.get(numDNodes),
                        node.getIdentity().getAddress(), node.getIdentity().getPort());
                SkipNode dNode = new SkipNode(dnID, lt);
                tableMap.put(numIDs.get(numDNodes), lt);
                node.insertDataNode(dNode);
                numDNodes++;
            }
        }

        // Check the correctness of the tables.
        for(SkipNode n : g.getNodes()) {
            tableCorrectnessCheck(n.getNumID(), n.getNameID(), n.getLookupTable());
            tableConsistencyCheck(tableMap, n);
        }

        underlays.forEach(Underlay::terminate);
    }

}
