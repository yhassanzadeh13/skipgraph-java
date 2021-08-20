package misc;

import java.util.AbstractList;
import java.util.Collection;
import java.util.Random;

public class Utils {
  public static Object randomIndex(AbstractList list, Random random){
    return list.get((Math.abs(random.nextInt()) % list.size()));
  }

  public static Object randomIndex(AbstractList list, Random random, int limit){
    if(limit <= 1) {
      return list.get(0);
    }
    return list.get((Math.abs(random.nextInt()) % limit));
  }
}
