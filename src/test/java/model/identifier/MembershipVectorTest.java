package model.identifier;

import io.ipfs.multibase.Multibase;
import model.skipgraph.SkipGraph;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import unittest.Fixtures;
import static model.identifier.Identifier.COMPARE_EQUAL;
import static model.identifier.Identifier.COMPARE_GREATER;
import static model.identifier.Identifier.COMPARE_LESS;
import static model.skipgraph.SkipGraph.IDENTIFIER_SIZE;

public class MembershipVectorTest {

  /**
   * Asserts MembershipVector constructor happy path as well as its getters.
   */
  @Test
  void TestMembershipVector_Create() {
    byte[] bytes = Fixtures.ByteArrayFixture(IDENTIFIER_SIZE);
    MembershipVector mv = new MembershipVector(bytes);

    Assertions.assertArrayEquals(bytes, mv.getBytes());

    String mvStr = Multibase.encode(Multibase.Base.Base58BTC, bytes);
    Assertions.assertEquals(mv.toString(), mvStr);

    // since identifiers are encoded using Base58BTC, all of them must
    // start with character `z`.
    Assertions.assertEquals('z', mvStr.charAt(0));
  }

  /**
   * Asserts MembershipVector constructor returns an illegal argument exception if input bytes size
   * is not exactly equal to skip graph identifier size.
   */
  @Test
  void MembershipVector_IllegitimateSize() {
    byte[] smallerBytes = Fixtures.ByteArrayFixture(IDENTIFIER_SIZE - 1);
    Assertions.assertThrows(IllegalArgumentException.class, () -> new MembershipVector(smallerBytes));

    byte[] biggerBytes = Fixtures.ByteArrayFixture(IDENTIFIER_SIZE + 1);
    Assertions.assertThrows(IllegalArgumentException.class, () -> new MembershipVector(biggerBytes));
  }

  /**
   * Asserts common prefix method of MembershipVectors. Generates two identifiers, and compares them using
   * comparison method.
   */
  @Test
  void TestMembershipVector_CommonPrefix() {
    byte[] biggerBytes = Fixtures.ByteArrayFixture(IDENTIFIER_SIZE);
    biggerBytes[0] = Byte.MAX_VALUE; // making sure that it starts with 1
    MembershipVector thisMV = new MembershipVector(biggerBytes);

    // equality
    Assertions.assertEquals(IDENTIFIER_SIZE * 8, thisMV.commonPrefix(thisMV));


    byte[] smallerBytes = Fixtures.ByteArrayFixture(IDENTIFIER_SIZE);
    smallerBytes[0] = Byte.MIN_VALUE; // making sure that it starts with zero
    Identifier smallerId = new Identifier(smallerBytes);
  }
}
