package underlay.javarmi;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import underlay.UnderlayTest;

/**
 * This test creates two Java RMI underlays on the host machine at different ports and checks the
 * connectivity between them. `sendMessage` and `terminate` tests are implemented in the `UnderlayTest` class.
 */
class JavaRMIUnderlayTest extends UnderlayTest {

    // Create two Java RMI underlays at different ports.
    @BeforeAll
    static void setUp() {
        // Construct the underlays.
        localUnderlay = new JavaRMIUnderlay();
        remoteUnderlay = new JavaRMIUnderlay();

        buildLayers(localUnderlay);
        buildLayers(remoteUnderlay);

        Assertions.assertTrue(localUnderlay.initialize(LOCAL_PORT + 2));
        Assertions.assertTrue(remoteUnderlay.initialize(REMOTE_PORT + 2));
    }

}