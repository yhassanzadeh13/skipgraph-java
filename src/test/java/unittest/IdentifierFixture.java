package unittest;

import java.util.ArrayList;

import model.identifier.Identifier;
import model.skipgraph.SkipGraph;

/**
 * Encapsulates utilities for a LightChain identifier.
 */
public class IdentifierFixture {
  public static Identifier newIdentifier() {
    byte[] bytes = Bytes.byteArrayFixture(SkipGraph.IDENTIFIER_SIZE);
    return new Identifier(bytes);
  }

  /**
   * Creates an arraylist of identifiers.
   *
   * @param count total number of identifiers.
   * @return array list of created identifiers.
   */
  public static ArrayList<Identifier> newIdentifiers(int count) {
    ArrayList<Identifier> identifiers = new ArrayList<>();

    for (int i = 0; i < 10; i++) {
      Identifier identifier = IdentifierFixture.newIdentifier();
      identifiers.add(identifier);
    }

    return identifiers;
  }
}
