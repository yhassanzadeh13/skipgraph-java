package model.identifier;

import io.ipfs.multibase.Multibase;
import model.skipgraph.SkipGraph;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import unittest.Fixtures;
import static model.identifier.Identifier.COMPARE_EQUAL;
import static model.identifier.Identifier.COMPARE_GREATER;
import static model.identifier.Identifier.COMPARE_LESS;

public class MembershipVectorTest {

  /**
   * Asserts MembershipVector constructor happy path as well as its getters.
   */
  @Test
  void TestMembershipVector_Create() {
    byte[] bytes = Fixtures.ByteArrayFixture(SkipGraph.IDENTIFIER_SIZE);
    MembershipVector mv = new MembershipVector(bytes);

    Assertions.assertArrayEquals(bytes, mv.getBytes());

    String mvStr = Multibase.encode(Multibase.Base.Base58BTC, bytes);
    Assertions.assertEquals(mv.toString(), mvStr);

    // since identifiers are encoded using Base58BTC, all of them must
    // start with character `z`.
    Assertions.assertEquals('z', mvStr.charAt(0));
  }

  /**
   * Asserts Identifier constructor returns an illegal argument exception if input bytes size
   * is not exactly equal to skip graph identifier size.
   */
  @Test
  void TestIdentifier_IllegitimateSize() {
    byte[] smallerBytes = Fixtures.ByteArrayFixture(SkipGraph.IDENTIFIER_SIZE - 1);
    Assertions.assertThrows(IllegalArgumentException.class, () -> new Identifier(smallerBytes));

    byte[] biggerBytes = Fixtures.ByteArrayFixture(SkipGraph.IDENTIFIER_SIZE + 1);
    Assertions.assertThrows(IllegalArgumentException.class, () -> new Identifier(biggerBytes));
  }

  /**
   * Asserts comparison method of identifiers. Generates two identifiers, and compares them using
   * comparison method.
   */
  @Test
  void TestIdentifier_Comparison() {
    byte[] smallerBytes = Fixtures.ByteArrayFixture(SkipGraph.IDENTIFIER_SIZE);
    smallerBytes[0] = Byte.MIN_VALUE; // making sure most significant bits are unset;
    Identifier smallerId = new Identifier(smallerBytes);

    byte[] biggerBytes = Fixtures.ByteArrayFixture(SkipGraph.IDENTIFIER_SIZE);
    biggerBytes[0] = Byte.MAX_VALUE;
    Identifier biggerId = new Identifier(biggerBytes);

    Assertions.assertEquals(COMPARE_LESS, smallerId.comparedTo(biggerId));
    Assertions.assertEquals(COMPARE_GREATER, biggerId.comparedTo(smallerId));
    Assertions.assertEquals(COMPARE_EQUAL, smallerId.comparedTo(smallerId));
    Assertions.assertEquals(COMPARE_EQUAL, biggerId.comparedTo(biggerId));
  }
}
