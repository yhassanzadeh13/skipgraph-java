package lookup;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import model.identifier.Identity;
import unittest.IdentityFixture;

import java.util.ArrayList;
import java.util.List;

public class ConcurrentLookupTableTest {

  protected static ConcurrentLookupTable concurrentLookupTable;
  protected static List<Identity> nodesToInsert;

  // Initializes lookup table.
  @BeforeEach
  void setUp() {
    int size = 30;
    concurrentLookupTable = new ConcurrentLookupTable(size, IdentityFixture.newIdentity());
    nodesToInsert = new ArrayList<>();

    for (int i = 1; i <= size; i++) {
      nodesToInsert.add(IdentityFixture.newIdentity());
    }
  }

  @Test
  void addRightNeighborsSequentially() {
    for (int i = 0; i < nodesToInsert.size(); i ++) {
      concurrentLookupTable.updateRight(nodesToInsert.get(i), i);
    }

    for (int i = 0; i < nodesToInsert.size(); i++) {
      Assertions.assertEquals(nodesToInsert.get(i), concurrentLookupTable.getRight(i));
    }

    Assertions.assertEquals(nodesToInsert.size(), nodesToInsert.size());
  }

//  @Test
//  void addRightNeighborsInOrderTest() {
//    for (int i = 0; i < nodesToInsert.size(); i++) {
//      concurrentBackupTable.addRightNode(nodesToInsert.get(i), 0);
//    }
//    Assertions.assertIterableEquals(nodesToInsert, concurrentBackupTable.getRights(0));
//  }
//
//
//  @Test
//  void addLeftNeighborsInReverseOrderTest() {
//    Collections.reverse(nodesToInsert);
//    for (int i = 0; i < nodesToInsert.size(); i++) {
//      concurrentBackupTable.addLeftNode(nodesToInsert.get(i), 0);
//    }
//    Assertions.assertIterableEquals(nodesToInsert, concurrentBackupTable.getLefts(0));
//  }
//
//  @Test
//  void addLeftNeighborsInOrderTest() {
//    for (int i = 0; i < nodesToInsert.size(); i++) {
//      concurrentBackupTable.addLeftNode(nodesToInsert.get(i), 0);
//    }
//    Collections.reverse(nodesToInsert);
//    Assertions.assertIterableEquals(nodesToInsert, concurrentBackupTable.getLefts(0));
//  }

}
