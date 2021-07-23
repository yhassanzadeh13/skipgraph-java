package lookup;

import java.io.Serializable;
import java.util.List;
import skipnode.SkipNodeIdentity;

/**
 * Represents a list of level-lists that is returned to a newly joined node by its neighbor. In case
 * of concurrent insertions (i.e., when ConcurrentBackupTable is used), this tentative table
 * contains every node in the skip graph at their maximum level with respect to the newly joined
 * node.
 */
public class TentativeTable implements Serializable {

  public final boolean complete;
  public final int specificLevel;
  public final List<List<SkipNodeIdentity>> neighbors;

  /**
   * Constructor for TentativeTable.
   *
   * @param complete boolean representing if complete or not.
   * @param specificLevel Integer representing the specific level.
   * @param neighbors neighbour table.
   */
  public TentativeTable(boolean complete, int specificLevel,
      List<List<SkipNodeIdentity>> neighbors) {
    this.complete = complete;
    this.specificLevel = specificLevel;
    this.neighbors = neighbors;
  }
}
