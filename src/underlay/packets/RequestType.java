package underlay.packets;

/**
 * Denotes the types of the requests that will be employed at the underlay layer.
 */
public enum RequestType {
    SearchByNameID,
    SearchByNumID,
    NameIDLevelSearch,
    UpdateLeftNode,
    UpdateRightNode
}
