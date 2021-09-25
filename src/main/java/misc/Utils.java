package misc;

import java.util.AbstractList;
import java.util.Random;

/**
 * General utilities for codebase.
 */
public class Utils {
  /**
   * Returns object of a random index in range of [0, limit] from the abstract list.
   *
   * @param list input list.
   * @param random random generator.
   * @return element of a random index from the list.
   */
  public static Object randomIndex(AbstractList list, Random random) {
    return list.get((Math.abs(random.nextInt()) % list.size()));
  }

  /**
   * Returns object of a random index from the abstract list.
   *
   * @param list input list.
   * @param random random generator.
   * @param limit maximum index can be returned exclusive.
   * @return element of a random index in range of [0, limit] from the list.
   */
  public static Object randomIndex(AbstractList list, Random random, int limit) {
    if (limit <= 1) {
      return list.get(0);
    }
    return list.get((Math.abs(random.nextInt()) % limit));
  }
}