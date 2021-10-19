package model.identifier;

import static model.identifier.SkipGraph.IDENTIFIER_SIZE;

/**
 * A class to automatically calculate nameId's size.
 */
public class NameId {
  private String nameID;

  /**
   * A class to automatically calculate nameId's size.
   *
   * @param nodes (total/maximum) number of nodes in Skip Graph
   * @return name ID size
   */
  public static int computeSize(int nodes) {
    return ((int) (Math.log(nodes) / Math.log(2)));
  }

  @org.jetbrains.annotations.NotNull
  private static String pretty(String nameID) {
    StringBuilder originalBuilder = new StringBuilder(nameID);
    while (originalBuilder.length() < IDENTIFIER_SIZE) {
      originalBuilder.insert(0, '0');
    }
    nameID = originalBuilder.toString();
    return nameID;
  }

  public static boolean validate(String nameID) {
    try {
      Integer.parseInt(nameID, 2);
    } catch (Exception e) {
      return false;
    }

    return true;
  }

  public void set(String nameID) throws IllegalArgumentException {
    if (!validate(nameID)) {
      throw new IllegalArgumentException("could not qualify input string as a name id: " + nameID);
    }
    if (nameID.length() > IDENTIFIER_SIZE) {
      throw new IllegalArgumentException("name id size beyond limit (" + IDENTIFIER_SIZE + "): " + nameID.length());
    }
    if (nameID.length() < IDENTIFIER_SIZE) {
      nameID = pretty(nameID);
    }
    this.nameID = nameID;
  }

  public String get() {
    return nameID;
  }

  /**
   * Returns common prefix length between this name ID and other name ID.
   *
   * @param other the other name ID.
   * @return common prefix length between 0 to name ID size;
   * @throws IllegalArgumentException if other name IDs is null or longer than name ID size.
   */
  public int commonPrefix(String other) throws IllegalArgumentException {
    if (this.nameID == null || other == null) {
      throw new IllegalArgumentException("cannot take common prefix of null name id(s)");
    }

    if (other.length() > IDENTIFIER_SIZE) {
      throw new
          IllegalArgumentException("cannot compute common prefix when other name ID is longer than legit size, expected: "
          + IDENTIFIER_SIZE + " got: " + other.length());
    }

    if (this.nameID.length() != other.length()) {
      throw new IllegalArgumentException("cannot take common prefix of different size name ids");
    }

    int i = 0;
    while (i < this.nameID.length() && this.nameID.charAt(i) == other.charAt(i)) {
      i++;
    }
    return i;
  }

  /**
   * Returns name ID as binary string.
   *
   * @return binary representation of name ID.
   */
  public String toString() {
    return this.nameID;
  }

  /**
   *
   * @param nameID
   * @return
   */
  public boolean isEqual(String nameID) {
    return this.commonPrefix(nameID) == this.nameID.length();
  }
}
