package misc;

import lookup.LookupTable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import skipnode.SkipNode;
import skipnode.SkipNodeIdentity;

class LocalSkipGraphTest {

  @Test
  void fourNodes() {
    LocalSkipGraph g = new LocalSkipGraph(4, "127.0.0.1", 9090, true);
    for (SkipNode n : g.getNodes()) {
      tableCorrectnessCheck(n.getNumId(), n.getNameId(), n.getLookupTable());
    }
  }

  @Test
  void eightNodes() {
    LocalSkipGraph g = new LocalSkipGraph(8, "127.0.0.1", 9090, true);
    for (SkipNode n : g.getNodes()) {
      tableCorrectnessCheck(n.getNumId(), n.getNameId(), n.getLookupTable());
    }
  }

  @Test
  void sixteenNodes() {
    LocalSkipGraph g = new LocalSkipGraph(16, "127.0.0.1", 9090, true);
    for (SkipNode n : g.getNodes()) {
      tableCorrectnessCheck(n.getNumId(), n.getNameId(), n.getLookupTable());
    }
  }

  // Checks the correctness of a lookup table owned by the node with the given identity parameters.
  static void tableCorrectnessCheck(int numID, String nameID, LookupTable table) {
    for (int i = 0; i < table.getNumLevels(); i++) {
      for (int j = 0; j < 2; j++) {
        SkipNodeIdentity neighbor = (j == 0) ? table.getLeft(i) : table.getRight(i);
        if (neighbor.equals(LookupTable.EMPTY_NODE)) {
          continue;
        }
        Assertions.assertTrue(SkipNodeIdentity.commonBits(neighbor.getNameId(), nameID) >= i);
      }
      SkipNodeIdentity leftNeighbor = table.getLeft(i);
      SkipNodeIdentity rightNeighbor = table.getRight(i);
      if (!leftNeighbor.equals(LookupTable.EMPTY_NODE)) {
        Assertions.assertTrue(leftNeighbor.getNumId() < numID);
      }
      if (!rightNeighbor.equals(LookupTable.EMPTY_NODE)) {
        Assertions.assertTrue(rightNeighbor.getNumId() > numID);
      }
    }
  }

}