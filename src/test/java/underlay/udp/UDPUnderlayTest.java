package underlay.udp;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import underlay.UnderlayTest;

/**
 * This test creates two UDP underlays on the host machine at different ports and checks the
 * connectivity between them. `sendMessage` and `terminate` tests are implemented in the `UnderlayTest` class.
 */
class UDPUnderlayTest extends UnderlayTest {
    @BeforeEach
    public void setup() {
        // Construct the underlays.
        this.localUnderlay = new UDPUnderlay();
        this.remoteUnderlay = new UDPUnderlay();

        buildLayers(this.localUnderlay);
        buildLayers(this.remoteUnderlay);

//        Assertions.assertTrue(this.localUnderlay.initialize(LOCAL_PORT + 4));
//        Assertions.assertTrue(this.remoteUnderlay.initialize(REMOTE_PORT + 4));
        initialize();
    }
}