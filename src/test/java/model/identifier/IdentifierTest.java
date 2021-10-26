package model.identifier;

import io.ipfs.multibase.Multibase;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import unittest.Fixtures;

public class IdentifierTest {

  @Test
  void TestIdentifier_Create(){
    byte[] bytes = Fixtures.ByteArrayFixture(SkipGraph.IDENTIFIER_SIZE);
    Identifier id = new Identifier(bytes);

    Assertions.assertArrayEquals(bytes, id.getBytes());

    String idStr = Multibase.encode(Multibase.Base.Base58BTC, bytes);
    Assertions.assertEquals(id.toString(), idStr);

    // since identifiers are encoded using Base58BTC, all of them must
    // start with character `z`.
    Assertions.assertEquals('z', idStr.charAt(0));
  }
}
