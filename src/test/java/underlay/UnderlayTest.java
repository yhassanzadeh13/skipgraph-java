package underlay;

import lookup.LookupTable;
import lookup.LookupTableFactory;
import middlelayer.MiddleLayer;
import org.junit.jupiter.api.*;
import skipnode.SkipNode;
import skipnode.SkipNodeInterface;
import underlay.packets.RequestType;
import underlay.packets.requests.*;

/**
 * This test creates two underlays on the machine at different ports and checks the
 * connectivity between them.
 */
public abstract class UnderlayTest {

    protected static final int LOCAL_PORT = 9090;
    protected static final int REMOTE_PORT = 9091;

    protected Underlay localUnderlay;
    protected Underlay remoteUnderlay;

    /**
     * Builds the middle layer and overlay on top of the given underlay so that it can
     * be used.
     * @param underlay underlay to be built.
     */
    protected static void buildLayers(Underlay underlay) {
        SkipNodeInterface overlay = new SkipNode(LookupTable.EMPTY_NODE, LookupTableFactory.createDefaultLookupTable(2));
        MiddleLayer middleLayer = new MiddleLayer(underlay, overlay);
        underlay.setMiddleLayer(middleLayer);
        overlay.setMiddleLayer(middleLayer);
    }

    @BeforeEach
    public abstract void setup();

    // Checks the message delivery for every request type between underlays.
    @Test
    void sendMessage() {
        // The address of the remote underlay.
        String remoteAddress = remoteUnderlay.getAddress();
        int remotePort = remoteUnderlay.getPort();

        // Check search by name ID request.
        Assertions.assertNotNull(localUnderlay.sendMessage(remoteAddress, remotePort, new SearchByNameIDRequest("")));
        // Check search by numerical ID request.
        Assertions.assertNotNull(localUnderlay.sendMessage(remoteAddress, remotePort, new SearchByNumIDRequest(0)));

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