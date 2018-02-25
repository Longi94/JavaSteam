package in.dragonbra.javasteam.steam.discovery;

import in.dragonbra.javasteam.TestBase;
import in.dragonbra.javasteam.networking.steam3.ProtocolTypes;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author lngtr
 * @since 2018-02-25
 */
public class ServerRecordTest extends TestBase {

    @Test
    public void nullIsNotEqual() {
        ServerRecord s = ServerRecord.createWebSocketServer("host:1");
        assertFalse(s.equals(null));
    }

    @Test
    public void differentProtocolsAreNotEqual() {
        ServerRecord l = ServerRecord.createServer("host", 1, ProtocolTypes.TCP);
        ServerRecord r = ServerRecord.createServer("host", 1, ProtocolTypes.WEB_SOCKET);

        assertFalse(l.equals(r));
        assertFalse(r.equals(l));
    }

    @Test
    public void differentEndPointsAreNotEqual() {
        ServerRecord l = ServerRecord.createServer("host", 1, ProtocolTypes.TCP);
        ServerRecord r = ServerRecord.createServer("host", 2, ProtocolTypes.TCP);

        assertFalse(l.equals(r));
        assertFalse(r.equals(l));
    }

    @Test
    public void differentEndPointsAndProtocolsAreNotEqual() {
        ServerRecord l = ServerRecord.createServer("host", 1, ProtocolTypes.TCP);
        ServerRecord r = ServerRecord.createServer("host", 2, ProtocolTypes.WEB_SOCKET);

        assertFalse(l.equals(r));
        assertFalse(r.equals(l));
    }

    @Test
    public void SameEndPointsAndProtocolsAreEqual() {
        ServerRecord l = ServerRecord.createServer("host", 1, ProtocolTypes.TCP);
        ServerRecord r = ServerRecord.createServer("host", 1, ProtocolTypes.TCP);

        assertTrue(l.equals(r));
        assertTrue(r.equals(l));

        assertEquals(l.hashCode(), r.hashCode());
    }
}