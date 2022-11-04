package underlay.tcp;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import underlay.UnderlayTest;

/**
 * This test creates two TCP underlays on the host machine at different ports and checks the
 * connectivity between them. `sendMessage` and `terminate` tests are implemented in the
 * `UnderlayTest` class.
 */
class TCPUnderlayTest extends UnderlayTest {

  // Create two TCP underlays at different ports.
  @BeforeAll
  static void setup() {
    // Construct the underlays.
    localUnderlay = new TcpUnderlay();
    remoteUnderlay = new TcpUnderlay();

    buildLayers(localUnderlay);
    buildLayers(remoteUnderlay);

    Assertions.assertTrue(localUnderlay.initialize(LOCAL_PORT));
    Assertions.assertTrue(remoteUnderlay.initialize(REMOTE_PORT));
  }
}