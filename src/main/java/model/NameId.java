
package model;

public class NameId {
    /**
     *
     * @param nodes (total/maximum) number of nodes in Skip Graph
     * @return name ID size
     */
    public static int computeSize(int nodes){
        return ((int) (Math.log(nodes)/Math.log(2)));
    }
}