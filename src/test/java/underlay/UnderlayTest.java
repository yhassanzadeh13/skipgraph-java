package underlay;

import lookup.LookupTable;
import lookup.LookupTableFactory;
import middlelayer.MiddleLayer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import skipnode.SkipNode;
import skipnode.SkipNodeInterface;
import underlay.packets.RequestType;
import underlay.packets.requests.*;

/**
 * This test creates two underlays on the machine at different ports and checks the
 * connectivity between them. Uses the default underlay implementation.
 */
public class UnderlayTest {

    protected static final int LOCAL_PORT = 9090;
    protected static final int REMOTE_PORT = 9091;

    protected static Underlay localUnderlay;
    protected static Underlay remoteUnderlay;

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

    // Initializes the underlays.
    @BeforeAll
    static void setUp() {
        localUnderlay = Underlay.newDefaultUnderlay();
        remoteUnderlay = Underlay.newDefaultUnderlay();

        buildLayers(localUnderlay);
        buildLayers(remoteUnderlay);

        Assertions.assertTrue(localUnderlay.initialize(LOCAL_PORT));
        Assertions.assertTrue(remoteUnderlay.initialize(REMOTE_PORT));
    }

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

        // TODO: implement for NameIDLevelSearchRequest
//        // Check level-based search by name ID request.
//        Assertions.assertNotNull(localUnderlay.sendMessage(remoteAddress, remotePort, new NameIDLevelSearchRequest(0, 0, "")));

        // Check left/right update requests.
        Assertions.assertNotNull(localUnderlay.sendMessage(remoteAddress, remotePort, new UpdateLeftNodeRequest(0, LookupTable.EMPTY_NODE)));
        Assertions.assertNotNull(localUnderlay.sendMessage(remoteAddress, remotePort, new UpdateRightNodeRequest(0, LookupTable.EMPTY_NODE)));
    }

    // Terminates the underlays.
    @AfterAll
    static void tearDown() {
        Assertions.assertTrue(localUnderlay.terminate());
        Assertions.assertTrue(remoteUnderlay.terminate());
    }
}