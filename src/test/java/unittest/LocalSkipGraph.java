package unittest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import lookup.ConcurrentLookupTable;
import lookup.LookupTable;
import middlelayer.MiddleLayer;
import model.identifier.Identifier;
import model.skipgraph.SkipGraph;
import org.junit.jupiter.api.Assertions;
import node.skipgraph.Node;
import model.identifier.Identity;
import underlay.Underlay;

/**
 * Represents a locally constructed skip-graph with correct lookup tables. The lookup tables are
 * built without utilizing the skip-graph join protocol, thus the skip-graphs constructed by this
 * class can be used while testing.
 */
public class LocalSkipGraph {
  private final List<Node> nodes;
  private final List<Underlay> underlays;

  /**
   * Constructor for LocalSkipGraph.
   *
   * @param size       Integer representing the size.
   * @param manualJoin Boolean representing if its manual join or not.
   */
  public LocalSkipGraph(int size, boolean manualJoin) {
    NetworkHub networkHub = new NetworkHub();
    this.underlays = new ArrayList<>(size);
    List<Identity> identities = new ArrayList<>(size);
    for (int i = 0; i < size; i++) {
      Underlay underlay = new MockUnderlay(networkHub);
      underlay.initialize(0);
      Assertions.assertTrue(underlay.getPort() > 0);
      underlays.add(underlay);
      identities.add(
        new Identity(
          IdentifierFixture.newIdentifier(),
          MembershipVectorFixture.newMembershipVector(),
          underlay.getAddress(),
          underlay.getPort()));
    }
    // Construct the lookup tables.
    List<LookupTable> lookupTables = new ArrayList<>(size);
    for (int i = 0; i < size; i++) {
      ConcurrentLookupTable lookupTable = new ConcurrentLookupTable(SkipGraph.IDENTIFIER_SIZE, identities.get(i));
      lookupTables.add(lookupTable);
    }


    // If manualJoin flag is set, then construct the lookup table manually,
    // i.e. without using the join protocol.
    if (manualJoin) {
      // Sort the identities by their identifier in ascending order.
      identities.sort((id1, id2) -> id1.getIdentifier().comparedTo(id2.getIdentifier()));
      // At each level...
      for (int l = 0; l < SkipGraph.IDENTIFIER_SIZE; l++) {
        // Check for the potential neighbours.
        for (int i = 0; i < size; i++) {
          Identity id1 = identities.get(i);
          LookupTable lt1 = lookupTables.get(i);
          for (int j = i + 1; j < size; j++) {
            Identity id2 = identities.get(j);
            LookupTable lt2 = lookupTables.get(j);
            // Connect the nodes at this level if they should be connected
            // according to their membership vector.
            if (id1.getMemVec().commonPrefix(id2.getMemVec()) >= l) {
              lt1.updateRight(id2, l);
              lt2.updateLeft(id1, l);
              break;
            }
          }
        }
      }
    }

    // Finally, construct the nodes.
    nodes = new ArrayList<>(size);
    for (int i = 0; i < size; i++) {
      Node node = new Node(identities.get(i), lookupTables.get(i));

      MiddleLayer middleLayer = new MiddleLayer(underlays.get(i), node);
      node.setMiddleLayer(middleLayer);
      underlays.get(i).setMiddleLayer(middleLayer);

      // Mark as inserted if lookup table was created manually.
      if (manualJoin) {
        node.insert(null, -1);
      }
      nodes.add(node);
    }
  }
  /**
   * Returns the list of nodes. Their middle layer needs to be assigned in order for them to be
   * usable.
   *
   * @return the list of nodes.
   */
  public List<Node> getNodes() {
    return nodes;
  }

  /**
   * Inserts the nodes in a randomized order. This should not be used when the local skip graph was
   * constructed with `manualJoin` flag set.
   */
  public void insertAllRandomized() {
    // Denotes the order of insertion.
    List<Node> list = new ArrayList<>(getNodes());
    // Randomize the insertion order.
    Collections.shuffle(list);
    list.get(0).insert(null, -1);
    // Insert the remaining nodes.
    for (int i = 1; i < list.size(); i++) {
      Node initiator = list.get(i - 1);
      list.get(i).insert(initiator.getIdentity().getAddress(), initiator.getIdentity().getPort());
    }
  }

  public Map<Identifier, LookupTable> identifierLookupTableMap() {
    return nodes.stream().collect(Collectors.toMap(Node::getIdentifier, Node::getLookupTable));
    // return idMap.entrySet().stream().collect(Collectors.toMap(e -> e.getKey().getIdentifier(), Map.Entry::getValue));
  }

  public void terminate() {
    for (Underlay underlay : underlays) {
      underlay.terminate();
    }
  }
}
