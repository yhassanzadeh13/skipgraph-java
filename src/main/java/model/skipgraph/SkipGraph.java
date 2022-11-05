package model.skipgraph;

import java.util.Arrays;

import model.identifier.Identifier;
import model.identifier.MembershipVector;

/**
 * Represents constant parameters belonging to the skip graph system.
 */
public class SkipGraph {
  /**
   * Size of name ID and numerical ID in bits.
   * It is set to 32 bytes which corresponds to 256 bits.
   */
  public static final int IDENTIFIER_SIZE = 32;

  /**
   * A reference for byte size that equals to 8 bits.
   */
  public static final int BYTE_SIZE = 8;

  // TODO: empty identifier must be hash("empty")
  public static Identifier getEmptyIdentifier() {
    byte[] allZero = new byte[SkipGraph.IDENTIFIER_SIZE];
    Arrays.fill(allZero, (byte) 0);
    return new Identifier(allZero);
  }

  // TODO: invalid identifier must be hash("invalid")
  public static Identifier getInvalidIdentifier() {
    byte[] allOne = new byte[SkipGraph.IDENTIFIER_SIZE];
    Arrays.fill(allOne, (byte) 1);
    return new Identifier(allOne);
  }

  // TODO: empty membership vector must be hash("empty")
  public static MembershipVector getEmptyMembershipVector() {
    byte[] allZero = new byte[SkipGraph.IDENTIFIER_SIZE];
    Arrays.fill(allZero, (byte) 0);
    return new MembershipVector(allZero);
  }

  // TODO: invalid membership vector must be hash("invalid")
  public static MembershipVector getInvalidMembershipVector() {
    byte[] allOne = new byte[SkipGraph.IDENTIFIER_SIZE];
    Arrays.fill(allOne, (byte) 1);
    return new MembershipVector(allOne);
  }
}
