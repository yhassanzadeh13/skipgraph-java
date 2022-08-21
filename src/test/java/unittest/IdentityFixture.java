package unittest;

import skipnode.SkipNodeIdentity;

public class IdentityFixture {
  public static SkipNodeIdentity newIdentity() {
    return new SkipNodeIdentity(
        IdentifierFixture.newIdentifier(),
        MembershipVectorFixture.newMembershipVector(),
        "0.0.0.0",
        0);
  }
}
