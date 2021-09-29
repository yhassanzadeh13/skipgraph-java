package model;

/**
 * A class to automatically calculate nameId's size.
 */
public class NameId {
  private String nameID;
  public final static int SIZE = 10;
  /**
   * A class to automatically calculate nameId's size.
   *
   * @param nodes (total/maximum) number of nodes in Skip Graph
   * @return name ID size
   */
  public static int computeSize(int nodes) {
    return ((int) (Math.log(nodes) / Math.log(2)));
  }

  public void set(String nameID) throws IllegalArgumentException {
    if(!validate(nameID)){
      throw new IllegalArgumentException("could not qualify input string as a name id: " + nameID);
    }
    if(nameID.length() > SIZE){
      throw new IllegalArgumentException("name id size beyond limit (" + SIZE + "): " + nameID.length());
    }
    if(nameID.length() < SIZE){
      nameID = pretty(nameID);
    }
    this.nameID = nameID;
  }

  @org.jetbrains.annotations.NotNull
  private static String pretty(String nameID){
    StringBuilder originalBuilder = new StringBuilder(nameID);
    while (originalBuilder.length() < SIZE) {
      originalBuilder.insert(0, '0');
    }
    nameID = originalBuilder.toString();
    return nameID;
  }

  public static boolean validate(String nameID){
    try{
      Integer.parseInt(nameID, 2);
    } catch (Exception e){
      return false;
    }

    return true;
  }

  public String get() {
    return nameID;
  }

  public int commonPrefix(String other) throws IllegalArgumentException{
    if (this.nameID == null || other == null) {
      throw new IllegalArgumentException("cannot take common prefix of null name id(s)");
    }
    if (this.nameID.length() != other.length()) {
      throw new IllegalArgumentException("cannot take common prefix of different size name ids");
    }

    int i = 0;
    while (i < this.nameID.length() && this.nameID.charAt(i) == other.charAt(i)) {
      i++;
    }
    return i;
  }

  public String toString(){
    return this.nameID;
  }

  public boolean isEqual(String nameID){
    return this.commonPrefix(nameID) == this.nameID.length();
  }
}
