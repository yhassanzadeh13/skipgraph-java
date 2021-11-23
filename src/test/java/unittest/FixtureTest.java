package unittest;

import misc.Utils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class FixtureTest {
  @Test
  public void TestByteArrayFixture_OneBitPrefix(){
    // starting with 1
    byte[] onePrefix = Fixtures.ByteArrayFixture("1", 10);
    Assertions.assertEquals(10, onePrefix.length);
    Assertions.assertTrue(Utils.toBinaryRepresentation(onePrefix[0]).startsWith("1"));

    // starting with 0
    byte[] zeroPrefix = Fixtures.ByteArrayFixture("0", 10);
    Assertions.assertEquals(10, zeroPrefix.length);
    Assertions.assertTrue(Utils.toBinaryRepresentation(zeroPrefix[0]).startsWith("0"));
  }

  @Test
  public void TestByteArrayFixture_TwoBitsPrefix(){
    // starting with 1
    // can't have 11 prefix due to byte range
    byte[] onePrefix = Fixtures.ByteArrayFixture("10", 10);
    Assertions.assertEquals(10, onePrefix.length);
    Assertions.assertTrue(Utils.toBinaryRepresentation(onePrefix[0]).startsWith("10"));

    // starting with 0
    byte[] zeroPrefix = Fixtures.ByteArrayFixture("00", 10);
    Assertions.assertEquals(10, zeroPrefix.length);
    Assertions.assertTrue(Utils.toBinaryRepresentation(zeroPrefix[0]).startsWith("00"));
  }

  @Test
  public void TestByteArrayFixture_MultipleBitsPrefix(){
    // starting with 1
    // can't have more preceding 1s in binary strings starting with 1 due
    // to byte limitation.
    byte[] onePrefix = Fixtures.ByteArrayFixture("1000", 10);
    Assertions.assertEquals(10, onePrefix.length);
    Assertions.assertTrue(Utils.toBinaryRepresentation(onePrefix[0]).startsWith("10"));

    // starting with 0
    byte[] zeroPrefix = Fixtures.ByteArrayFixture("0101", 10);
    Assertions.assertEquals(10, zeroPrefix.length);
    Assertions.assertTrue(Utils.toBinaryRepresentation(zeroPrefix[0]).startsWith("0101"));
  }

  @Test
  public void TestByteArrayFixture_OneBytePrefix(){
    // starting with 1
    // can't have more preceding 1s in binary strings starting with 1 due
    // to byte limitation.
    //
    // Note: byte parser does not accept max value of 10000000 (-128), since up to
    // last step it considers it positive (128), which is beyond bytes range [-128, 127].
    // Hence, we pass a 7 bits string starting with 1, i.e., 1000000x, and the fixture will
    // return the complete byte.
    byte[] onePrefix = Fixtures.ByteArrayFixture("1000000", 10);
    Assertions.assertEquals(10, onePrefix.length);
    Assertions.assertTrue(Utils.toBinaryRepresentation(onePrefix[0]).startsWith("10000000"));

    // starting with 0
    byte[] zeroPrefix = Fixtures.ByteArrayFixture("01010000", 10);
    Assertions.assertEquals(10, zeroPrefix.length);
    Assertions.assertTrue(Utils.toBinaryRepresentation(zeroPrefix[0]).startsWith("01010000"));
  }


}
