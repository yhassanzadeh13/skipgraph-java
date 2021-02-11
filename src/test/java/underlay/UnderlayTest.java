package underlay;

import lookup.LookupTable;
import lookup.LookupTableFactory;
import middlelayer.MiddleLayer;
import org.apache.log4j.Level;
import org.apache.log4j.spi.Configurator;
import org.junit.jupiter.api.*;
import skipnode.SkipNode;
import skipnode.SkipNodeInterface;
import underlay.packets.RequestType;
import underlay.packets.requests.*;

import java.net.BindException;
import java.util.Random;

/**
 * This test creates two underlays on the machine at different ports and checks the
 * connectivity between them.
 */
public abstract class UnderlayTest {
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

    protected void initialize(){
        Assertions.assertTrue(initializer(localUnderlay));
        Assertions.assertTrue(initializer(remoteUnderlay));
    }

    private boolean initializer(Underlay underlay){
        Random random = new Random();
        final int MAX_PORT_NUM = 9999;
        final int MAX_RETRY_ITERATIONS = 1000; // tries at most 1000 randomly ports
        int port = MAX_PORT_NUM; // initializes port at a point
        int iterations = 0;

        // tries random available ports for 1000 iterations
        while(!underlay.initialize(port) && iterations < MAX_RETRY_ITERATIONS){
            port = random.nextInt(MAX_PORT_NUM) + 1;
            iterations++;
        }

        if(iterations >= 1000){
            return false;
        }
        return true;
    }

    // Terminates the underlays.
    @AfterEach
    void tearDown() {
//        Assertions.assertTrue(localUnderlay.terminate());
//        Assertions.assertTrue(remoteUnderlay.terminate());
    }
}