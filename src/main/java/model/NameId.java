package model;

/**
 * A class to automatically calculate nameId's size.
 */

public class NameId {
  /**
   * A class to automatically calculate nameId's size.
   *
   * @param nodes (total/maximum) number of nodes in Skip Graph
   * @return name ID size
   */
  public static int computeSize(int nodes) {
    return (int) Math.ceil( Math.log(nodes) / Math.log(2));
  }

}
