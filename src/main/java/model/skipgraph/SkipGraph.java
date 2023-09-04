package model.skipgraph;

import java.util.Arrays;

import model.identifier.Identifier;
import model.identifier.MembershipVector;

/**
 * Represents constant parameters belonging to the skip graph system.
 */
public class SkipGraph {
  /**
   * Size of identifier and membership vector in bits.
   * It is set to 32 bytes which corresponds to 256 bits.
   */
  public static final int IDENTIFIER_SIZE = 32;

  /**
   * A reference for byte size that equals to 8 bits.
   */
  public static final int BYTE_SIZE = 8;

  // TODO: empty identifier must be hash("empty")

  /**
   * Empty identifier.
   *
   * @return empty identifier (all zero for now).
   */
  public static Identifier getEmptyIdentifier() {
    byte[] allZero = new byte[SkipGraph.IDENTIFIER_SIZE];
    Arrays.fill(allZero, (byte) 0);
    return new Identifier(allZero);
  }

  /**
   * Invalid identifier.
   *
   * @return all one identifier which acts as an invalid identifier.
   */
  // TODO: invalid identifier must be hash("invalid")
  public static Identifier getInvalidIdentifier() {
    byte[] allOne = new byte[SkipGraph.IDENTIFIER_SIZE];
    Arrays.fill(allOne, (byte) 1);
    return new Identifier(allOne);
  }

  // TODO: empty membership vector must be hash("empty")

  /**
   * Empty membership vector.
   *
   * @return empty membership vector (all zero for now).
   */
  public static MembershipVector getEmptyMembershipVector() {
    byte[] allZero = new byte[SkipGraph.IDENTIFIER_SIZE];
    Arrays.fill(allZero, (byte) 0);
    return new MembershipVector(allZero);
  }

  // TODO: invalid membership vector must be hash("invalid")

  /**
   * Membership vector that represents an invalid membership vector.
   *
   * @return all one membership vector which acts as an invalid membership vector.
   */
  public static MembershipVector getInvalidMembershipVector() {
    byte[] allOne = new byte[SkipGraph.IDENTIFIER_SIZE];
    Arrays.fill(allOne, (byte) 1);
    return new MembershipVector(allOne);
  }
}
