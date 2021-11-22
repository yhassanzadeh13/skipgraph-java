package model.identifier;

import io.ipfs.multibase.Multibase;
import misc.Utils;
import model.skipgraph.SkipGraph;
import static model.skipgraph.SkipGraph.IDENTIFIER_SIZE;

/**
 * MembershipVector represents the membership vector of a skip graph node, i.e.,
 * what is known as name ID in literatures:
 * <p>
 * For more info on membership vector see these:
 * Hassanzadeh-Nazarabadi, Yahya, Alptekin Küpçü, and Öznur Özkasap. "Locality aware skip graph."
 * 2015 IEEE 35th International Conference on Distributed Computing Systems Workshops. IEEE, 2015.
 * <p>
 * Hassanzadeh-Nazarabadi, Yahya, Alptekin Küpçü, and Öznur Özkasap. "Interlaced: Fully decentralized churn
 * stabilization for skip graph-based dhts." Journal of Parallel and Distributed Computing 149 (2021): 13-28.
 */
public class MembershipVector {
  /**
   * Base58BTC representation of membership vector.
   */
  private final String membershipVector;
  /**
   * Byte representation of membership vector.
   */
  private final byte[] byteRepresentation;

  /**
   * Initializes the membership vector with the byte array.
   *
   * @param membershipVector the byte array membership vector.
   * @throws IllegalArgumentException if size of input byte array is not identical to the identifier
   *                                  size of skip graph.
   */
  public MembershipVector(byte[] membershipVector) throws IllegalArgumentException {
    if (membershipVector.length != SkipGraph.IDENTIFIER_SIZE) {
      throw new IllegalArgumentException("membership vector must be exactly the legitimate size "
          + "(" + SkipGraph.IDENTIFIER_SIZE + "): " + membershipVector.length);
    }

    this.byteRepresentation = membershipVector;
    this.membershipVector = pretty(membershipVector);
  }

  /**
   * A class to automatically calculate nameId's size.
   *
   * @param nodes (total/maximum) number of nodes in Skip Graph
   * @return membership vector size
   */
  @Deprecated
  public static int computeSize(int nodes) {
    return ((int) (Math.log(nodes) / Math.log(2)));
  }

  /**
   * Converts MembershipVector from its byte representation to Base58BTC.
   *
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
   * Returns common prefix length between this membership vector and other.
   *
   * @param other the other membership vector.
   * @return common prefix length in bits, i.e., between 0 to 8 * IDENTIFIER_LENGTH;
   */
  public int commonPrefix(MembershipVector other) {
    int i = 0; // index of first discrepancy

    for (; i < IDENTIFIER_SIZE; i++) {
      if (this.byteRepresentation[i] != other.byteRepresentation[i]) {
        break;
      }
    }

    if (i == IDENTIFIER_SIZE) {
      return IDENTIFIER_SIZE * 8; // full match in bits
    }

    // scanning bits by bits of the first different byte
    String ithis = Utils.toBinaryRepresentation(this.byteRepresentation[i]);
    String iother = Utils.toBinaryRepresentation(other.byteRepresentation[i]);

    for (int j = 0; j < ithis.length(); j++) {
      if (ithis.charAt(j) != iother.charAt(j)) {
        return 8 * i + j;
      }
    }

    throw new IllegalStateException("failed to find common prefix: " + this + " and " + other);
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
