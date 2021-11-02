package unittest;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import misc.Utils;

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

  public static byte[]ByteArrayFixture(String prefix, int length){
    if(prefix.length() > length){
      throw new IllegalArgumentException("prefix (" + prefix + ") must be less than or equal to length ("+ length + ")");
    }

    byte[] fixtureByte = new byte[length];

    // converting prefix to byte
    int index = 0;
    List<String> prefixSplit = Utils.splitEqually(prefix, 8);
    for(String str: prefixSplit){
      fixtureByte[index++] = Byte.parseByte(str, 2);
    }

    int remainSize = length - index;
    byte[] remainBytes = ByteArrayFixture(remainSize);
    System.arraycopy(remainBytes, 0, fixtureByte, index, remainSize);

    return fixtureByte;
  }


  /**
   * 
   * @param prefix
   * @return
   */
  public static byte ByteFixture(String prefix){
    if(prefix.length() > 8){
      throw new IllegalArgumentException("prefix (" + prefix + ") must be less than or equal to byte length ("+ 8 + ")");
    }

    StringBuilder bStr = new StringBuilder(prefix);
    // converting prefix to byte
    for(int i = 0; i < 8 - prefix.length(); i++){
      if(random.nextBoolean()){
        bStr.append("1");
      } else {
        bStr.append("0");
      }
    }

    return Byte.parseByte(bStr.toString(), 2);
  }



}
