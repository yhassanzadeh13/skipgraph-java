package model.identifier;

public class Identifier {
  /**
   * The numerical Id.
   */
  private String identifier;

  /***
   * converts identifier from its binary representation to its hexadecimal one.
   * @param identifier input identifier.
   * @return base 16 representation of identifier.
   */
  @org.jetbrains.annotations.NotNull
  private static String pretty(String identifier) {
    // Converts binary representation of identifier to decimal first,
    // and then the decimal representation to hex.
    // TODO it can support up to 32 bit integers. Making it support 256 ones.
    int decimal = Integer.parseInt(identifier,2);
    return Integer.toString(decimal,16);
  }

  /**
   * returns true if input parameter represents a valid identifier, otherwise, false.
   * @param identifier input identifier to be validated.
   * @return true for valid identifiers and false otherwise.
   */
  public static boolean validate(String identifier) {
    // currently, we only support 32 bits integers.
    if (identifier.length() > 32) {
      return false;
    }

    // must be parseable to integer.
    try {
      Integer.parseInt(identifier, 2);
    } catch (Exception e) {
      return false;
    }

    return true;
  }

  /**
   * Sets identifier of the node.
   * @param identifier binary string representation of identifier.
   * @throws IllegalArgumentException if identifier is not binary parsable, null,
   *                                  or its size is beyond the system limit.
   * @throws IllegalStateException    if identifier is already set.
   */
  public void set(String identifier) throws IllegalArgumentException, IllegalStateException {
    if (this.identifier != null){
      throw new IllegalStateException("attempt on setting an already set identifier");
    }
    if (!validate(identifier)) {
      throw new IllegalArgumentException("could not qualify input string as a numerical id: " + identifier);
    }
    if (identifier.length() > SkipGraph.IDENTIFIER_SIZE) {
      throw new IllegalArgumentException("name id size beyond limit (" + SkipGraph.IDENTIFIER_SIZE + "): " + identifier.length());
    }
    if (identifier.length() < SkipGraph.IDENTIFIER_SIZE) {
      identifier = pretty(identifier);
    }
    this.identifier = identifier;
  }

  /**
   * Returns identifier of the node.
   * @return identifier of the node.
   */
  public String get() {
    return this.identifier;
  }



  /**
   * Returns identifier as a binary string.
   *
   * @return binary representation of identifier of the node.
   */
  public String toString() {
    return this.identifier;
  }

  /**
   * Compares this identifier with the other identifier. 
   * @param other represents other identifier to compared to.
   * @return 0 if two identifiers are equal, 1 if this identifier is greater than other, -1 if other identifier
   *         is greater than this.
   */
  public int comparedTo(Identifier other) {
    int thisValue = Integer.parseInt(this.identifier,2);
    int otherValue = Integer.parseInt(other.identifier,2);

    if(thisValue == otherValue){
      return 0;
    }
    else if (thisValue > otherValue){
      return 1;
    }
    else return -1;
  }
}
