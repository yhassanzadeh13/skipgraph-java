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
    byte[] biggerBytes = Fixtures.MaxByteArrayFixture(IDENTIFIER_SIZE); // all 1s
    MembershipVector thisMV = new MembershipVector(biggerBytes);

    // equality
    Assertions.assertEquals(IDENTIFIER_SIZE * 8, thisMV.commonPrefix(thisMV));


    byte[] smallerBytes = Fixtures.MinByteArrayFixture(IDENTIFIER_SIZE); // all zeros
    MembershipVector noCommonPrefixMV = new MembershipVector(smallerBytes);
    // zero bit common prefix
    Assertions.assertEquals(0, thisMV.commonPrefix(noCommonPrefixMV));

    // one bit common prefix
    byte[] firstBitSet = Fixtures.MinByteArrayFixture(IDENTIFIER_SIZE); // all zeros
    firstBitSet[0] = 1; // 1....
    MembershipVector oneBitCommonPrefix = new MembershipVector(firstBitSet);
    Assertions.assertEquals(1, thisMV.commonPrefix(oneBitCommonPrefix));

    // two bits common prefix
    byte[] twoBitsSet = Fixtures.MinByteArrayFixture(IDENTIFIER_SIZE); // all zeros
    twoBitsSet[0] = 64; // 11....
    MembershipVector twoBitsCommonPrefix = new MembershipVector(twoBitsSet);
    Assertions.assertEquals(2, thisMV.commonPrefix(twoBitsCommonPrefix));
  }
}
