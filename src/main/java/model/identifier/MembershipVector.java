package model.identifier;

import io.ipfs.multibase.Multibase;
import model.skipgraph.SkipGraph;
import static model.skipgraph.SkipGraph.IDENTIFIER_SIZE;

/**
 * MembershipVector represents the membership vector of a skip graph node, i.e.,
 * what is known as name ID in literatures:
 *
 * For more info on membership vector see these:
 * Hassanzadeh-Nazarabadi, Yahya, Alptekin Küpçü, and Öznur Özkasap. "Locality aware skip graph."
 * 2015 IEEE 35th International Conference on Distributed Computing Systems Workshops. IEEE, 2015.
 *
 * Hassanzadeh-Nazarabadi, Yahya, Alptekin Küpçü, and Öznur Özkasap. "Interlaced: Fully decentralized churn
 * stabilization for skip graph-based dhts." Journal of Parallel and Distributed Computing 149 (2021): 13-28.
 */
public class MembershipVector {
  /**
   * Base58BTC representation of membership vector
   */
  private final String membershipVector;
  /**
   * Byte representation of membership vector.
   */
  private final byte[] byteRepresentation;

  public MembershipVector(byte[] membershipVector) throws IllegalArgumentException{
    if (membershipVector.length != SkipGraph.IDENTIFIER_SIZE) {
      throw new IllegalArgumentException("membership vector must be exactly the legitimate size " +
          "(" + SkipGraph.IDENTIFIER_SIZE + "): " + membershipVector.length);
    }

    this.byteRepresentation = membershipVector;
    this.membershipVector = pretty(membershipVector);
  }

  /**
   * A class to automatically calculate nameId's size.
   *
   * @param nodes (total/maximum) number of nodes in Skip Graph
   * @return name ID size
   */
  @Deprecated
  public static int computeSize(int nodes) {
    return ((int) (Math.log(nodes) / Math.log(2)));
  }

  /***
   * Converts MembershipVector from its byte representation to Base58BTC.
   * @param membershipVector input membership vector in byte representation.
   * @return Base58BTC representation of membershipVector.
   */
  @org.jetbrains.annotations.NotNull
  private static String pretty(byte[] membershipVector) {
    return Multibase.encode(Multibase.Base.Base58BTC, membershipVector);
  }


  /**
   * Returns membership vector of the node as byte representation.
   *
   * @return membership vector of the node as byte representation.
   */
  public byte[] getBytes() {
    return this.byteRepresentation;
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
   * Returns string representation of membership vector in Base58BTC.
   *
   * @return string representation of membership vector in Base58BTC.
   */
  public String toString() {
    return this.membershipVector;
  }

}
