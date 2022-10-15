package skipnode;

import lookup.ConcurrentLookupTable;
import lookup.LookupTable;
import middlelayer.MiddleLayer;
import model.identifier.Identifier;
import model.identifier.MembershipVector;
import unittest.IdentifierFixture;
import unittest.LocalSkipGraph;
import org.junit.jupiter.api.Test;
import underlay.Underlay;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static unittest.LocalSkipGraph.prependToLength;
import static skipnode.SkipNodeTest.tableConsistencyCheck;
import static skipnode.SkipNodeTest.tableCorrectnessCheck;

public class DataNodeTest {

  static int STARTING_PORT = 8000;
  static int NODES = 8;
  static int DATANODESPERNODE = 3;

  @Test
  void testDataNodes() {
    // First, construct the main underlays.
    List<Underlay> underlays = new ArrayList<>(NODES);
    for (int i = 0; i < NODES; i++) {
      Underlay underlay = Underlay.newDefaultUnderlay();
      underlay.initialize(STARTING_PORT + i);
      underlays.add(underlay);
    }

    // Then, construct the local skip graph without manually constructing the lookup tables.
    int nameIdSize = ((int) (Math.log(NODES * (DATANODESPERNODE + 1)) / Math.log(2)));
    LocalSkipGraph g = new LocalSkipGraph(NODES, underlays.get(0).getAddress(), STARTING_PORT,
        false, nameIdSize);

    // Create the middle layers.
    for (int i = 0; i < NODES; i++) {
      MiddleLayer middleLayer = new MiddleLayer(underlays.get(i), g.getNodes().get(i));

      // Assign the middle layer to the underlay & overlay.
      underlays.get(i).setMiddleLayer(middleLayer);
      g.getNodes().get(i).setMiddleLayer(middleLayer);
    }

    // Now, insert every node in a randomized order.
    g.insertAll();

    Map<Identifier, LookupTable> tableMap = g.identifierLookupTableMap();
    // Check the correctness of the tables.
    for (SkipNode n : g.getNodes()) {
      tableCorrectnessCheck(n.getIdentity().getIdentifier(), n.getIdentity().getMembershipVector(), n.getLookupTable());
      tableConsistencyCheck(tableMap, n);
    }



    for (SkipNodeInterface node : g.getNodes()) {
      for (int i = 0; i < DATANODESPERNODE; i++) {
        Identifier identifier = IdentifierFixture.newIdentifier();
        MembershipVector membershipVector = new MembershipVector(identifier.getBytes());

        SkipNodeIdentity dnID = new SkipNodeIdentity(
            identifier,
            membershipVector,
            node.getIdentity().getAddress(),
            node.getIdentity().getPort());
        LookupTable lookupTable = new ConcurrentLookupTable(nameIdSize, dnID);
        SkipNode dNode = new SkipNode(dnID, lookupTable);
        tableMap.put(identifier, lookupTable);
        node.insertDataNode(dNode);
      }
    }

    // Check the correctness of the tables.
    for (SkipNode n : g.getNodes()) {
      tableCorrectnessCheck(n.getIdentity().getIdentifier(), n.getIdentity().getMembershipVector(), n.getLookupTable());
      tableConsistencyCheck(tableMap, n);
    }

    underlays.forEach(Underlay::terminate);
  }

}
