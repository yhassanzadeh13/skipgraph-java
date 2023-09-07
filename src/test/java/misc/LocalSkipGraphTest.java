package misc;

import lookup.LookupTable;
import model.identifier.Identifier;
import model.identifier.MembershipVector;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import skipnode.SkipNode;
import skipnode.SkipNodeIdentity;
import unittest.LocalSkipGraph;

class LocalSkipGraphTest {

  // Checks the correctness of a lookup table owned by the node with the given identity parameters.
  static void tableCorrectnessCheck(Identifier identifier, MembershipVector membershipVector, LookupTable table) {
    for (int i = 0; i < table.getNumLevels(); i++) {
      for (int j = 0; j < 2; j++) {
        SkipNodeIdentity neighbor = (j == 0) ? table.getLeft(i) : table.getRight(i);
        if (neighbor.equals(LookupTable.EMPTY_NODE)) {
          continue;
        }
        Assertions.assertTrue(neighbor.getMemVec().commonPrefix(membershipVector) >= i);
      }
      SkipNodeIdentity leftNeighbor = table.getLeft(i);
      SkipNodeIdentity rightNeighbor = table.getRight(i);
      if (!leftNeighbor.equals(LookupTable.EMPTY_NODE)) {
        Assertions.assertEquals(Identifier.COMPARE_LESS, leftNeighbor.getIdentifier().comparedTo(identifier));
      }
      if (!rightNeighbor.equals(LookupTable.EMPTY_NODE)) {
        Assertions.assertEquals(Identifier.COMPARE_GREATER, rightNeighbor.getIdentifier().comparedTo(identifier));
      }
    }
  }

  @Test
  void fourNodes() {
    LocalSkipGraph g = new LocalSkipGraph(4, true);
    for (SkipNode n : g.getNodes()) {
      tableCorrectnessCheck(n.getIdentity().getIdentifier(), n.getIdentity().getMemVec(), n.getLookupTable());
    }
  }

  @Test
  void eightNodes() {
    LocalSkipGraph g = new LocalSkipGraph(8,  true);
    for (SkipNode n : g.getNodes()) {
      tableCorrectnessCheck(n.getIdentity().getIdentifier(), n.getIdentity().getMemVec(), n.getLookupTable());
    }
  }

  @Test
  void sixteenNodes() {
    LocalSkipGraph g = new LocalSkipGraph(16, true);
    for (SkipNode n : g.getNodes()) {
      tableCorrectnessCheck(n.getIdentity().getIdentifier(), n.getIdentity().getMemVec(), n.getLookupTable());
    }
  }

}