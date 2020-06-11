package underlay;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import underlay.packets.GenericRequest;
import underlay.packets.RequestType;

import java.net.Inet4Address;
import java.net.UnknownHostException;

/**
 * This test creates two underlays on the machine at different ports and checks the
 * connectivity between them. Uses the Java RMI adapter for now.
 */
class UnderlayTest {

    private static final int LOCAL_PORT = 9090;
    private static final int REMOTE_PORT = 9091;

    private static Underlay localUnderlay;
    private static Underlay remoteUnderlay;

    private static String localIP;

    // Create two underlays at different ports.
    @BeforeAll
    static void setUp() {
        // Construct the underlays.
        localUnderlay = new Underlay(LOCAL_PORT);
        remoteUnderlay = new Underlay(REMOTE_PORT);

        try {
            localIP = Inet4Address.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    // Checks the message delivery for every request type between underlays.
    @Test
    void sendMessage() {
        // The address of the remote underlay.
        String remoteAddress = localIP + ":" + REMOTE_PORT;

        // Check search by name ID request.
        GenericRequest r = new GenericRequest();
        r.addParameter("targetNameID", "");
        Assertions.assertNotNull(localUnderlay.sendMessage(remoteAddress, RequestType.SearchByNameID, r));
        // Check search by numerical ID request.
        r = new GenericRequest();
        r.addParameter("targetNumID", 0);
        Assertions.assertNotNull(localUnderlay.sendMessage(remoteAddress, RequestType.SearchByNumID, r));
        // Check level-based search by name ID request.
        r = new GenericRequest();
        r.addParameter("level", 0);
        r.addParameter("targetNameID", "");
        Assertions.assertNotNull(localUnderlay.sendMessage(remoteAddress, RequestType.NameIDLevelSearch, r));
        // Check left/right update requests.
        r = new GenericRequest();
        r.addParameter("level", 0);
        r.addParameter("newValue", "");
        Assertions.assertNotNull(localUnderlay.sendMessage(remoteAddress, RequestType.UpdateLeftNode, r));
        Assertions.assertNotNull(localUnderlay.sendMessage(remoteAddress, RequestType.UpdateRightNode, r));
    }
}