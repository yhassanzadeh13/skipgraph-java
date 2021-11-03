package unittest;

import misc.Utils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class FixtureTest {
  @Test
  public void TestByteArrayFixture_OneBitPrefix(){
    byte[] onePrefix = Fixtures.ByteArrayFixture("0", 10);
    Assertions.assertEquals(10, onePrefix.length);
    System.out.println(Utils.toBinaryRepresentation(onePrefix[0]));
    Assertions.assertTrue(Utils.toBinaryRepresentation(onePrefix[0]).startsWith("0"));
  }
}
