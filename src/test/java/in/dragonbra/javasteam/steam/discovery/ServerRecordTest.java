package in.dragonbra.javasteam.steam.discovery;

import in.dragonbra.javasteam.TestBase;
import in.dragonbra.javasteam.networking.steam3.ProtocolTypes;
import org.junit.Test;

import java.net.InetSocketAddress;
import java.util.EnumSet;

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

    @Test
    public void canTryCreateSocketServer() {
        ServerRecord record = ServerRecord.tryCreateSocketServer("127.0.0.1:1234");

        assertNotNull(record);
        assertEquals(new InetSocketAddress("127.0.0.1", 1234), record.getEndpoint());
        assertEquals(EnumSet.of(ProtocolTypes.TCP, ProtocolTypes.UDP), record.getProtocolTypes());

        record = ServerRecord.tryCreateSocketServer("192.168.0.1:5678");

        assertNotNull(record);
        assertEquals(new InetSocketAddress("192.168.0.1", 5678), record.getEndpoint());
        assertEquals(EnumSet.of(ProtocolTypes.TCP, ProtocolTypes.UDP), record.getProtocolTypes());
    }

    @Test
    public void cannotTryCreateSocketServer() {
        ServerRecord record;

        record = ServerRecord.tryCreateSocketServer("127.0.0.1");
        assertNull(record);

        record = ServerRecord.tryCreateSocketServer("127.0.0.1:123456789");
        assertNull(record);

        record = ServerRecord.tryCreateSocketServer("127.0.0.1:-1234");
        assertNull(record);

        record = ServerRecord.tryCreateSocketServer("127.0.0.1:notanint");
        assertNull(record);

        record = ServerRecord.tryCreateSocketServer("volvopls.valvesoftware.com:1234");
        assertNull(record);
    }
}