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
import underlay.packets.GenericRequest;
import underlay.packets.RequestType;
import underlay.packets.UpdateRequest;

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
        SkipNodeInterface overlay = new SkipNode(LookupTable.EMPTY_NODE, LookupTableFactory.createDefaultLookupTable());
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
        GenericRequest r = new GenericRequest();
        r.addParameter("targetNameID", "");
        Assertions.assertNotNull(localUnderlay.sendMessage(remoteAddress, remotePort, RequestType.SearchByNameID, r));
        // Check search by numerical ID request.
        r = new GenericRequest();
        r.addParameter("targetNumID", 0);
        Assertions.assertNotNull(localUnderlay.sendMessage(remoteAddress, remotePort, RequestType.SearchByNumID, r));
        // Check level-based search by name ID request.
        r = new GenericRequest();
        r.addParameter("level", 0);
        r.addParameter("targetNameID", "");
        Assertions.assertNotNull(localUnderlay.sendMessage(remoteAddress, remotePort, RequestType.NameIDLevelSearch, r));
        // Check left/right update requests.
        UpdateRequest u = new UpdateRequest(0, LookupTable.EMPTY_NODE);
        Assertions.assertNotNull(localUnderlay.sendMessage(remoteAddress, remotePort, RequestType.UpdateLeftNode, u));
        Assertions.assertNotNull(localUnderlay.sendMessage(remoteAddress, remotePort, RequestType.UpdateRightNode, u));
    }

    // Terminates the underlays.
    @AfterAll
    static void tearDown() {
        Assertions.assertTrue(localUnderlay.terminate());
        Assertions.assertTrue(remoteUnderlay.terminate());
    }
}