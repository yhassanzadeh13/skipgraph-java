package model.identifier;


import java.util.Arrays;
import io.ipfs.multibase.Multibase;

/**
 * Identifier represents the main identifier of a skip graph node, i.e.,
 * what is known as numerical ID in literatures:
 *
 * For more info on numerical ID (i.e., identifier) see these:
 * Hassanzadeh-Nazarabadi, Yahya, Alptekin Küpçü, and Öznur Özkasap. "Locality aware skip graph."
 * 2015 IEEE 35th International Conference on Distributed Computing Systems Workshops. IEEE, 2015.
 *
 * Hassanzadeh-Nazarabadi, Yahya, Alptekin Küpçü, and Öznur Özkasap. "Interlaced: Fully decentralized churn
 * stabilization for skip graph-based dhts." Journal of Parallel and Distributed Computing 149 (2021): 13-28.
 */
public class Identifier {

  /**
   * Base58BTC representation of identifier.
   */
  private final String identifier;
  /**
   * Byte representation of identifier.
   */
  private final byte[] byteRepresentation;

  public Identifier(byte[] identifier) throws IllegalArgumentException{
    if (identifier.length != SkipGraph.IDENTIFIER_SIZE) {
      throw new IllegalArgumentException("identifier must be exactly the legitimate size " +
          "(" + SkipGraph.IDENTIFIER_SIZE + "): " + identifier.length);
    }

    this.byteRepresentation = identifier;
    this.identifier = pretty(identifier);
  }

  /***
   * Converts identifier from its byte representation to Base58BTC.
   * @param identifier input identifier in byte representation.
   * @return Base58BTC representation of identifier.
   */
  @org.jetbrains.annotations.NotNull
  private static String pretty(byte[] identifier) {
    return Multibase.encode(Multibase.Base.Base58BTC, identifier);
  }

  /**
   * Returns identifier of the node.
   *
   * @return identifier of the node.
   */
  public byte[] getBytes() {
    return this.byteRepresentation;
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
   *
   * @param other represents other identifier to compared to.
   * @return 0 if two identifiers are equal, 1 if this identifier is greater than other, -1 if other identifier
   * is greater than this.
   */
  public int comparedTo(Identifier other) {
    return Arrays.compare(this.byteRepresentation, other.byteRepresentation);
  }
}
