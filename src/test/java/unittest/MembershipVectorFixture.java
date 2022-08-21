package unittest;

import model.identifier.MembershipVector;
import model.skipgraph.SkipGraph;

public class MembershipVectorFixture {
  public static MembershipVector newMembershipVector() {
    byte[] bytes = Bytes.byteArrayFixture(SkipGraph.IDENTIFIER_SIZE);
    return new MembershipVector(bytes);
  }
}
