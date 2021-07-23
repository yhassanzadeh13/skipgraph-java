package skipnode;

import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.locks.Lock;
import log.Log4jLogger;
import lookup.ConcurrentBackupTable;
import org.apache.logging.log4j.LogManager;

/**
 * Node stash processor.
 */
public class NodeStashProcessor implements Runnable {

  private final LinkedBlockingDeque<SkipNodeIdentity> nodeStashRef;
  private final ConcurrentBackupTable backupTableRef;
  private final SkipNodeIdentity ownIdentity;
  private final Lock nodeStashLock;

  public boolean running = true;

  private static final Log4jLogger logger = new Log4jLogger(
      LogManager.getLogger(NodeStashProcessor.class));

  /**
   * Constructor for NodeStashProcessor.
   *
   * @param nodeStash nodeStash instance.
   * @param backupTableRef backup table instance.
   * @param ownIdentity nodes own identity.
   * @param nodeStashLock node stash lock.
   */
  public NodeStashProcessor(LinkedBlockingDeque<SkipNodeIdentity> nodeStash,
      ConcurrentBackupTable backupTableRef,
      SkipNodeIdentity ownIdentity, Lock nodeStashLock) {
    this.nodeStashRef = nodeStash;
    this.backupTableRef = backupTableRef;
    this.ownIdentity = ownIdentity;
    this.nodeStashLock = nodeStashLock;
  }

  @Override
  public void run() {
    while (running) {
      SkipNodeIdentity n = null;
      try {
        n = nodeStashRef.take();
      } catch (InterruptedException e) {
        logger.fatal()
            .Exception(e)
            .Int("num_id", this.ownIdentity.getNumID())
            .Msg("NodeStashProcessor could not take");
        continue;
      }
      if (n.equals(ownIdentity)) {
        continue;
      }
      int level = SkipNodeIdentity.commonBits(n.getNameID(), ownIdentity.getNameID());
      if (n.getNumID() < ownIdentity.getNumID()
          && !backupTableRef.getLefts(level).contains(n)) {
        for (int j = level; j >= 0; j--) {
          backupTableRef.addLeftNode(n, j);
        }
      } else if (n.getNumID() >= ownIdentity.getNumID()
          && !backupTableRef.getRights(level).contains(n)) {
        for (int j = level; j >= 0; j--) {
          backupTableRef.addRightNode(n, j);
        }
      }
    }
  }
}
