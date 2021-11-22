package misc;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * General utilities for codebase.
 */
public class Utils {
  /**
   * Returns object of a random index in range of [0, limit] from the abstract list.
   *
   * @param list   input list.
   * @param random random generator.
   * @return element of a random index from the list.
   */
  public static Object randomIndex(AbstractList list, Random random) {
    return list.get((Math.abs(random.nextInt()) % list.size()));
  }

  /**
   * Returns object of a random index from the abstract list.
   *
   * @param list   input list.
   * @param random random generator.
   * @param limit  maximum index can be returned exclusive.
   * @return element of a random index in range of [0, limit] from the list.
   */
  public static Object randomIndex(AbstractList list, Random random, int limit) {
    if (limit <= 1) {
      return list.get(0);
    }
    return list.get((Math.abs(random.nextInt()) % limit));
  }

  /**
   * Coverts byte b to 8 bits binary representation.
   *
   * @param b byte.
   * @return 8-bits binary representation.
   */
  public static String toBinaryRepresentation(byte b) {
    return String.format("%8s", Integer.toBinaryString(b & 0xFF)).replace(' ', '0');
  }

  /**
   * Splits string into equal string chunks of size.
   *
   * @param text input string.
   * @param size chunk sizes.
   * @return list of chunks.
   */
  public static List<String> splitEqually(String text, int size) {
    // Give the list the right capacity to start with. You could use an array
    // instead if you wanted.
    List<String> ret = new ArrayList<String>((text.length() + size - 1) / size);

    for (int start = 0; start < text.length(); start += size) {
      ret.add(text.substring(start, Math.min(text.length(), start + size)));
    }
    return ret;
  }
}
