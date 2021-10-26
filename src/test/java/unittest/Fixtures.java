package unittest;

import java.util.Random;

/**
 * Contains fixtures used for developing unit tests.
 */
public class Fixtures {
  private final static Random random = new Random();

  /**
   * Generates a random byte array.
   * @param length length of byte array.
   * @return random byte array.
   */
  public static byte[] ByteArrayFixture(int length){
    byte[] arr = new byte[length];
    random.nextBytes(arr);
    return arr;
  }

}
