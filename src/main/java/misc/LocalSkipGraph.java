package misc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import lookup.ConcurrentLookupTable;
import lookup.LookupTable;
import lookup.LookupTableFactory;
import model.NameId;
import skipnode.SkipNode;
import skipnode.SkipNodeIdentity;

/**
 * Represents a locally constructed skip-graph with correct lookup tables. The lookup tables are
 * built without utilizing the skip-graph join protocol, thus the skip-graphs constructed by this
 * class can be used while testing.
 */
public class LocalSkipGraph {

  private final List<SkipNode> skipNodes;

  public LocalSkipGraph(int size, String localAddress, int startingPort, boolean manualJoin) {
    this(size, localAddress, startingPort, manualJoin, NameId.computeSize(size));
  }

  /**
   * Constructor for LocalSkipGraph.
   *
   * @param size         Integer representing the size.
   * @param localAddress String representing the local address.
   * @param startingPort Integer representing the starting port.
   * @param manualJoin   Boolean representing if its manual join or not.
   * @param nameIdSize   Integer representing the manual id size.
   */
  public LocalSkipGraph(
      int size, String localAddress, int startingPort, boolean manualJoin, int nameIdSize) {
    // Create the numerical IDs.
    List<Integer> numIds = new ArrayList<>(size);
    for (int i = 0; i < size; i++) {
      numIds.add(i);
    }
    // Create the name IDs.
    List<String> nameIds =
        numIds.stream()
            .map(numID -> prependToLength(Integer.toBinaryString(numID), nameIdSize))
            .collect(Collectors.toList());
    // Randomly assign name IDs.
    Collections.shuffle(nameIds);
    // nameIds.forEach(x -> System.out.print(x + " "));

    // Create the identities.
    List<SkipNodeIdentity> identities = new ArrayList<>(size);
    for (int i = 0; i < size; i++) {
      identities.add(
          new SkipNodeIdentity(nameIds.get(i), numIds.get(i), localAddress, startingPort + i));
    }
    // Construct the lookup tables.
    List<LookupTable> lookupTables = new ArrayList<>(size);
    for (int i = 0; i < size; i++) {
      ConcurrentLookupTable lookupTable = new ConcurrentLookupTable(nameIdSize, identities.get(i));
      lookupTables.add(lookupTable);
    }


    // If manualJoin flag is set, then construct the lookup table manually,
    // i.e. without using the join protocol.
    if (manualJoin) {
      // At each level...
      for (int l = 0; l < nameIdSize; l++) {
        // Check for the potential neighbours.
        for (int i = 0; i < size; i++) {
          SkipNodeIdentity id1 = identities.get(i);
          LookupTable lt1 = lookupTables.get(i);
          for (int j = i + 1; j < size; j++) {
            SkipNodeIdentity id2 = identities.get(j);
            LookupTable lt2 = lookupTables.get(j);
            // Connect the nodes at this level if they should be connected
            // according to their name ID.
            if (SkipNodeIdentity.commonBits(id1.getNameId(), id2.getNameId()) >= l) {
              lt1.updateRight(id2, l);
              lt2.updateLeft(id1, l);
              break;
            }
          }
        }
      }
    }
    // Finally, construct the nodes.
    skipNodes = new ArrayList<>(size);
    for (int i = 0; i < size; i++) {
      SkipNode skipNode = new SkipNode(identities.get(i), lookupTables.get(i));
      // Mark as inserted if lookup table was created manually.
      if (manualJoin) {
        skipNode.insert(null, -1);
      }
      skipNodes.add(skipNode);
    }
  }

  /**
   * Prepends `0`s on the beginning of the given string until the desired length is reached.
   *
   * @param original     the original string to prepend `0`s on.
   * @param targetLength the desired length.
   * @return the prepended string.
   */
  public static String prependToLength(String original, int targetLength) {
    StringBuilder originalBuilder = new StringBuilder(original);
    while (originalBuilder.length() < targetLength) {
      originalBuilder.insert(0, '0');
    }
    original = originalBuilder.toString();
    return original;
  }

  /**
   * Returns the list of nodes. Their middle layer needs to be assigned in order for them to be
   * usable.
   *
   * @return the list of nodes.
   */
  public List<SkipNode> getNodes() {
    return skipNodes;
  }

  /**
   * Invokes the insertion protocol on every node. This should not be used when the local skip graph
   * was constructed with `manualJoin` flag set.
   */
  public void insertAll() {
    getNodes().get(0).insert(null, -1);
    // Insert the remaining nodes.
    for (int i = 1; i < getNodes().size(); i++) {
      SkipNode initiator = getNodes().get(i - 1);
      getNodes()
          .get(i)
          .insert(initiator.getIdentity().getAddress(), initiator.getIdentity().getPort());
    }
  }

  /**
   * Inserts the nodes in a randomized order. This should not be used when the local skip graph was
   * constructed with `manualJoin` flag set.
   */
  public void insertAllRandomized() {
    // Denotes the order of insertion.
    List<SkipNode> list = new ArrayList<>(getNodes());
    // Randomize the insertion order.
    Collections.shuffle(list);
    list.get(0).insert(null, -1);
    // Insert the remaining nodes.
    for (int i = 1; i < list.size(); i++) {
      SkipNode initiator = list.get(i - 1);
      list.get(i).insert(initiator.getIdentity().getAddress(), initiator.getIdentity().getPort());
    }
  }
}
