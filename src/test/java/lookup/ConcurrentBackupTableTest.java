package lookup;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import skipnode.SkipNodeIdentity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ConcurrentBackupTableTest {

  protected static ConcurrentBackupTable backupTable;
  protected static List<SkipNodeIdentity> nodesToInsert;

  // Initializes the backup table.
  @BeforeEach
  void setUp() {
    backupTable = new ConcurrentBackupTable(30, new SkipNodeIdentity("0000", 1, "None", -1));
    nodesToInsert = new ArrayList<>();

    for (int i = 1; i < 10; i++) {
      SkipNodeIdentity sn = new SkipNodeIdentity("0000", i, "None", -1);
      nodesToInsert.add(sn);
    }
  }

  @Test
  void addRightNeighborsInReverseOrderTest() {
    for (int i = nodesToInsert.size() - 1; i >= 0; i--) {
      backupTable.addRightNode(nodesToInsert.get(i), 0);
    }
    Assertions.assertIterableEquals(nodesToInsert, backupTable.getRights(0));
    Assertions.assertEquals(nodesToInsert.size(), nodesToInsert.size());
  }

  @Test
  void addRightNeighborsInOrderTest() {
    for (int i = 0; i < nodesToInsert.size(); i++) {
      backupTable.addRightNode(nodesToInsert.get(i), 0);
    }
    Assertions.assertIterableEquals(nodesToInsert, backupTable.getRights(0));
  }


  @Test
  void addLeftNeighborsInReverseOrderTest() {
    Collections.reverse(nodesToInsert);
    for (int i = 0; i < nodesToInsert.size(); i++) {
      backupTable.addLeftNode(nodesToInsert.get(i), 0);
    }
    Assertions.assertIterableEquals(nodesToInsert, backupTable.getLefts(0));
  }

  @Test
  void addLeftNeighborsInOrderTest() {
    for (int i = 0; i < nodesToInsert.size(); i++) {
      backupTable.addLeftNode(nodesToInsert.get(i), 0);
    }
    Collections.reverse(nodesToInsert);
    Assertions.assertIterableEquals(nodesToInsert, backupTable.getLefts(0));
  }

}
