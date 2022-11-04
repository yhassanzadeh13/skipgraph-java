package model.identifier;


import java.io.Serializable;
import java.util.Arrays;

import io.ipfs.multibase.Multibase;
import model.skipgraph.SkipGraph;

/**
 * Identifier represents the main identifier of a skip graph node, i.e.,
 * what is known as numerical ID in literatures:
 * For more info on numerical ID (i.e., identifier) see these:
 * Hassanzadeh-Nazarabadi, Yahya, Alptekin Küpçü, and Öznur Özkasap.
 * "Locality aware skip graph."
 * 2015 IEEE 35th International Conference on Distributed Computing Systems Workshops.
 * IEEE, 2015.
 * Hassanzadeh-Nazarabadi, Yahya, Alptekin Küpçü, and Öznur Özkasap.
 * "Interlaced: Fully decentralized churn stabilization for skip graph-based dhts."
 * Journal of Parallel and Distributed Computing 149 (2021): 13-28.
 */
public class Identifier implements Serializable {

  public static final int COMPARE_GREATER = 1;
  public static final int COMPARE_LESS = -1;
  public static final int COMPARE_EQUAL = 0;

  /**
   * Base58BTC representation of identifier.
   */
  private final String identifier;
  /**
   * Byte representation of identifier.
   */
  private final byte[] byteRepresentation;

  /**
   * Constructor of identifier, initializes identifier from the input byte array.
   *
   * @param identifier byte array representation of identifier.
   * @throws IllegalArgumentException if byte array size does not match identifier size of system.
   */
  public Identifier(byte[] identifier) throws IllegalArgumentException {
    if (identifier.length != SkipGraph.IDENTIFIER_SIZE) {
      throw new IllegalArgumentException("identifier must be exactly the legitimate size " + "(" + SkipGraph.IDENTIFIER_SIZE + "): " + identifier.length);
    }

    this.byteRepresentation = identifier;
    this.identifier = pretty(identifier);
  }

  /**
   * Converts identifier from its byte representation to Base58BTC.
   *
   * @param identifier input identifier in byte representation.
   * @return Base58BTC representation of identifier.
   */
  @org.jetbrains.annotations.NotNull
  private static String pretty(byte[] identifier) {
    return Multibase.encode(Multibase.Base.Base58BTC, identifier);
  }

  /**
   * Returns identifier of the node as byte representation.
   *
   * @return identifier of the node.
   */
  public byte[] getBytes() {
    return this.byteRepresentation;
  }


  /**
   * Returns string representation of identifier in Base58BTC.
   *
   * @return string representation of identifier in Base58BTC.
   */
  public String toString() {
    return this.identifier;
  }

  /**
   * Compares this identifier with the other identifier.
   *
   * @param other represents other identifier to compared to.
   * @return 0 if two identifiers are equal, 1 if this identifier is greater than other,
   * -1 if other identifier is greater than this.
   */
  public int comparedTo(Identifier other) {
    int result = Arrays.compare(this.byteRepresentation, other.byteRepresentation);
    return Integer.compare(result, 0);
  }

  public boolean equals(Identifier other) {
    return Arrays.equals(this.byteRepresentation, other.byteRepresentation);
  }

  public boolean isLessThan(Identifier other) {
    return comparedTo(other) == COMPARE_LESS;
  }

  public boolean isGreaterThan(Identifier other) {
    return comparedTo(other) == COMPARE_GREATER;
  }
}
