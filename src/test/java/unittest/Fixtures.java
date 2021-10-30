package unittest;

import java.util.Arrays;
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

  /**
   * Generates a byte array with all values at minimum byte value, i.e., -127.
   * @param length length of byte array.
   * @return byte array.
   */
  public static byte[] MinByteArrayFixture(int length){
    byte[] arr = new byte[length];
    Arrays.fill(arr, Byte.MIN_VALUE);
    return arr;
  }

  /**
   * Generates a byte array with all values at maximum byte value, i.e., 128.
   * @param length length of byte array.
   * @return byte array.
   */
  public static byte[] MaxByteArrayFixture(int length){
    byte[] arr = new byte[length];
    Arrays.fill(arr, Byte.MAX_VALUE);
    return arr;
  }

}
