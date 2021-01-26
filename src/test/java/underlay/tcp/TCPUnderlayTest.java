package underlay.tcp;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import underlay.UnderlayTest;

/**
 * This test creates two TCP underlays on the host machine at different ports and checks the
 * connectivity between them. `sendMessage` and `terminate` tests are implemented in the `UnderlayTest` class.
 */
class TCPUnderlayTest extends UnderlayTest {
    // Create two TCP underlays at different ports.
    @BeforeEach
    public void setup() {
        // Construct the underlays.
        localUnderlay = new TCPUnderlay();
        remoteUnderlay = new TCPUnderlay();

        buildLayers(localUnderlay);
        buildLayers(remoteUnderlay);

        Assertions.assertTrue(localUnderlay.initialize(LOCAL_PORT + 6));
        Assertions.assertTrue(remoteUnderlay.initialize(REMOTE_PORT + 6));
    }
}