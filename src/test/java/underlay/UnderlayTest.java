package underlay;

import lookup.ConcurrentLookupTable;
import lookup.LookupTable;
import middlelayer.MiddleLayer;
import org.junit.jupiter.api.*;
import skipnode.SkipNode;
import skipnode.SkipNodeInterface;
import underlay.packets.requests.*;
import unittest.IdentifierFixture;
import unittest.MembershipVectorFixture;

/**
 * This test creates two underlays on the machine at different ports and checks the connectivity
 * between them. Uses the default underlay implementation.
 */
public class UnderlayTest {
  protected static final int LOCAL_PORT = 0;
  protected static final int REMOTE_PORT = 0;

  protected static Underlay localUnderlay;
  protected static Underlay remoteUnderlay;

  /**
   * Builds the middle layer and overlay on top of the given underlay so that it can be used.
   *
   * @param underlay underlay to be built.
   */
  protected static void buildLayers(Underlay underlay) {
    SkipNodeInterface overlay = new SkipNode(LookupTable.EMPTY_NODE,
        new ConcurrentLookupTable(2, LookupTable.EMPTY_NODE));
    MiddleLayer middleLayer = new MiddleLayer(underlay, overlay);
    underlay.setMiddleLayer(middleLayer);
    overlay.setMiddleLayer(middleLayer);
  }

  // Initializes the underlays.
  @BeforeEach
  void setup() {
    localUnderlay = Underlay.newDefaultUnderlay();
    remoteUnderlay = Underlay.newDefaultUnderlay();

    buildLayers(localUnderlay);
    buildLayers(remoteUnderlay);

    Assertions.assertTrue(localUnderlay.initialize(LOCAL_PORT));
    Assertions.assertTrue(remoteUnderlay.initialize(REMOTE_PORT));
  }

  // Checks the message delivery for every request type between underlays.
  // TODO: does this test actually test anything?
  @Test
  void sendMessage() {
    // The address of the remote underlay.
    String remoteAddress = remoteUnderlay.getAddress();
    int remotePort = remoteUnderlay.getPort();

    // Check search by membership vector request.
    Assertions.assertNotNull(
        localUnderlay.sendMessage(remoteAddress, remotePort, new SearchByMembershipVectorRequest(MembershipVectorFixture.newMembershipVector())));
    // Check search by identifier request.
    Assertions.assertNotNull(
        localUnderlay.sendMessage(remoteAddress, remotePort, new SearchByIdentifierRequest(IdentifierFixture.newIdentifier())));
    // Check left/right update requests.
    Assertions.assertNotNull(localUnderlay.sendMessage(remoteAddress, remotePort, new UpdateLeftNodeRequest(0, LookupTable.EMPTY_NODE)));
    Assertions.assertNotNull(localUnderlay.sendMessage(remoteAddress, remotePort, new UpdateRightNodeRequest(0, LookupTable.EMPTY_NODE)));
  }

  // Terminates the underlays.
  @AfterEach
  void tearDown() {
    Assertions.assertTrue(localUnderlay.terminate());
    Assertions.assertTrue(remoteUnderlay.terminate());
  }
}