package lookup;

import skipnode.SkipNodeIdentity;

/** Factory class for creating lookup tables. */
public class LookupTableFactory {

  // TODO Move these perhaps to a settings file of some sort
  public static final String DEFAULT_FACTORY_TYPE = "Lookup";

  public static LookupTable createDefaultLookupTable(int numLevels, SkipNodeIdentity o) {
    return createLookupTable(DEFAULT_FACTORY_TYPE, numLevels, o);
  }

  /**
   * Method responsible for creating lookup table.
   *
   * @param type backup table type: either backup or lookup.
   * @param tableSize Integer representing table size.
   * @return new instance of lookup table.
   */
  public static LookupTable createLookupTable(String type, int tableSize, SkipNodeIdentity o) {
    if (type.equals("Backup")) {
      return new ConcurrentBackupTable(tableSize, o);
    }
    return new ConcurrentLookupTable(tableSize, o);
  }
}
