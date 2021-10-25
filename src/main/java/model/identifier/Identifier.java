package model.identifier;


import java.util.Arrays;
import io.ipfs.multibase.Multibase;

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
    if (identifier.length == SkipGraph.IDENTIFIER_SIZE) {
      throw new IllegalArgumentException("identifier must be exactly the legitimate size (" + SkipGraph.IDENTIFIER_SIZE + "): " + identifier.length);
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
   *
   * @param other represents other identifier to compared to.
   * @return 0 if two identifiers are equal, 1 if this identifier is greater than other, -1 if other identifier
   * is greater than this.
   */
  public int comparedTo(Identifier other) {
    return Arrays.compare(this.byteRepresentation, other.byteRepresentation);
  }
}
