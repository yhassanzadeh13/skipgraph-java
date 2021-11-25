package network.udp;

import org.junit.jupiter.api.Assertions;
import network.UnderlayTest;

/**
 * This test creates two UDP underlays on the host machine at different ports and checks the
 * connectivity between them. `sendMessage` and `terminate` tests are implemented in the
 * `UnderlayTest` class.
 */
class UDPUnderlayTest extends UnderlayTest {

  // @BeforeEach
  void setUp() {
    // Construct the underlays.
    localUnderlay = new UdpUnderlay();
    remoteUnderlay = new UdpUnderlay();

    buildLayers(localUnderlay);
    buildLayers(remoteUnderlay);

    Assertions.assertTrue(localUnderlay.initialize(LOCAL_PORT + 4));
    Assertions.assertTrue(remoteUnderlay.initialize(REMOTE_PORT + 4));
  }
}