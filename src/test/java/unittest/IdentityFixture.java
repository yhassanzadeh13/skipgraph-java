package unittest;

import skipnode.Identity;

public class IdentityFixture {
  public static Identity newIdentity() {
    return new Identity(
        IdentifierFixture.newIdentifier(),
        MembershipVectorFixture.newMembershipVector(),
        "0.0.0.0",
        0);
  }
}
