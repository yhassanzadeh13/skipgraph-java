package network.packets;

/** Denotes the types of the requests that will be employed at the underlay layer. */
public enum RequestType {
  SearchByNameId,
  SearchByNameIDRecursive,
  SearchByNumId,
  NameIdLevelSearch,
  UpdateLeftNode,
  UpdateRightNode,
  GetLeftNode,
  GetRightNode,
  AcquireNeighbors,
  FindLadder,
  AnnounceNeighbor,
  IsAvailable,
  GetLeftLadder,
  Increment,
  Injection,
  GetRightLadder,
  AcquireLock,
  ReleaseLock,
  GetIdentity
}
