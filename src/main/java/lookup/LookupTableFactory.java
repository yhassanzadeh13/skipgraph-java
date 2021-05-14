package lookup;

import skipnode.SkipNodeIdentity;

public class LookupTableFactory {
    // TODO Move these perhaps to a settings file of some sort
    public static final String DEFAULT_FACTORY_TYPE = "Lookup";

    public static LookupTable createDefaultLookupTable(int numLevels, SkipNodeIdentity o) {
        return createLookupTable(DEFAULT_FACTORY_TYPE, numLevels, o);
    }

    public static LookupTable createLookupTable(String type, int tableSize, SkipNodeIdentity o) {
        if (type.equals("Backup")) {
            return new ConcurrentBackupTable(tableSize, o);
        }
        return new ConcurrentLookupTable(tableSize, o);
    }
}
