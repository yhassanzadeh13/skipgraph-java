package misc;

import lookup.ConcurrentLookupTable;
import lookup.LookupTable;
import skipnode.SkipNode;
import skipnode.SkipNodeIdentity;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Represents a locally constructed skip-graph with correct lookup tables. The lookup tables are built without
 * utilizing the skip-graph join protocol, thus the skip-graphs constructed by this class can be used while testing.
 */
public class LocalSkipGraph {

    private final List<SkipNode> skipNodes;

    public LocalSkipGraph(int size, String localAddress, int startingPort) {
        int nameIDSize = ((int) (Math.log(size)/Math.log(2)));
        // Create the numerical IDs.
        List<Integer> numIDs = new ArrayList<>(size);
        for(int i = 0; i < size; i++) numIDs.add(i);
        // Create the name IDs.
        List<String> nameIDs = numIDs.stream()
                .map(numID -> prependToLength(Integer.toBinaryString(numID), nameIDSize))
                .collect(Collectors.toList());
        // Create the identities.
        List<SkipNodeIdentity> identities = new ArrayList<>(size);
        for(int i = 0; i < size; i++) {
            identities.add(new SkipNodeIdentity(nameIDs.get(i), numIDs.get(i), localAddress, startingPort + i));
        }
        // Construct the lookup tables.
        List<LookupTable> lookupTables = new ArrayList<>(size);
        for(int i = 0; i < size; i++) lookupTables.add(new ConcurrentLookupTable(nameIDSize));
        // At each level...
        for(int l = 0; l < nameIDSize; l++) {
            // Check for the potential neighborships.
            for(int i = 0; i < size; i++) {
                SkipNodeIdentity id1 = identities.get(i);
                LookupTable lt1 = lookupTables.get(i);
                for(int j = i + 1; j < size; j++) {
                    SkipNodeIdentity id2 = identities.get(j);
                    LookupTable lt2 = lookupTables.get(j);
                    // Connect the nodes at this level if they should be connected according to their name ID.
                    if (SkipNodeIdentity.commonBits(id1.getNameID(), id2.getNameID()) >= l) {
                        lt1.UpdateRight(id2, l);
                        lt2.UpdateLeft(id1, l);
                        break;
                    }
                }
            }
        }
        // Finally, construct the skip nodes.
        skipNodes = new ArrayList<>(size);
        for(int i = 0; i < size; i++) skipNodes.add(new SkipNode(identities.get(i), lookupTables.get(i)));
    }

    /**
     * Returns the list of nodes. Their middle layer needs to be assigned in order for them to be usable.
     * @return the list of nodes.
     */
    public List<SkipNode> getNodes() {
        return skipNodes;
    }

    /**
     * Prepends `0`s on the beginning of the given string until the desired length is reached.
     * @param original the original string to prepend `0`s on.
     * @param targetLength the desired length.
     * @return the prepended string.
     */
    private static String prependToLength(String original, int targetLength) {
        StringBuilder originalBuilder = new StringBuilder(original);
        while(originalBuilder.length() < targetLength) {
            originalBuilder.insert(0, '0');
        }
        original = originalBuilder.toString();
        return original;
    }

}
