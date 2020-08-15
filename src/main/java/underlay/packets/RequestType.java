package underlay.packets;

/**
 * Denotes the types of the requests that will be employed at the underlay layer.
 */
public enum RequestType {
    SearchByNameID,
    SearchByNameIDRecursive,
    SearchByNumID,
    NameIDLevelSearch,
    UpdateLeftNode,
    UpdateRightNode,
    GetLeftNode,
    GetRightNode,
    GetPotentialNeighbors,
    FindLadder
}
