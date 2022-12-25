package in.dragonbra.javasteam.steam.discovery;

import in.dragonbra.javasteam.TestBase;
import in.dragonbra.javasteam.networking.steam3.ProtocolTypes;
import in.dragonbra.javasteam.steam.steamclient.configuration.SteamConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author lngtr
 * @since 2018-02-25
 */
public class SmartCMServerListTest extends TestBase {

    private SmartCMServerList serverList;

    @BeforeEach
    public void setUp() {
        SteamConfiguration configuration = SteamConfiguration.create(b -> b.withDirectoryFetch(false));
        serverList = new SmartCMServerList(configuration);
    }

    @Test
    public void tryMergeWithList_AddsToHead_AndMovesExisting() {
        serverList.getAllEndPoints();

        List<ServerRecord> seedList = new ArrayList<>();
        seedList.add(ServerRecord.createSocketServer(new InetSocketAddress(InetAddress.getLoopbackAddress(), 27025)));
        seedList.add(ServerRecord.createSocketServer(new InetSocketAddress(InetAddress.getLoopbackAddress(), 27026)));
        seedList.add(ServerRecord.createSocketServer(new InetSocketAddress(InetAddress.getLoopbackAddress(), 27027)));
        seedList.add(ServerRecord.createSocketServer(new InetSocketAddress(InetAddress.getLoopbackAddress(), 27028)));

        serverList.replaceList(seedList);

        assertEquals(4, seedList.size());

        List<ServerRecord> listToReplace = new ArrayList<>();
        listToReplace.add(ServerRecord.createSocketServer(new InetSocketAddress(InetAddress.getLoopbackAddress(), 27015)));
        listToReplace.add(ServerRecord.createSocketServer(new InetSocketAddress(InetAddress.getLoopbackAddress(), 27035)));
        listToReplace.add(ServerRecord.createSocketServer(new InetSocketAddress(InetAddress.getLoopbackAddress(), 27105)));

        serverList.replaceList(listToReplace);

        List<ServerRecord> addresses = serverList.getAllEndPoints();

        assertEquals(3, addresses.size());
        assertEquals(listToReplace.get(0), addresses.get(0));
        assertEquals(listToReplace.get(1), addresses.get(1));
        assertEquals(listToReplace.get(2), addresses.get(2));
    }

    @Test
    public void getNextServerCandidate_ReturnsNull_IfListIsEmpty() {
        ServerRecord endPoint = serverList.getNextServerCandidate(ProtocolTypes.TCP);
        assertNull(endPoint);
    }

    @Test
    public void getNextServerCandidate_ReturnsServer_IfListHasServers() {
        serverList.getAllEndPoints();

        ServerRecord record = ServerRecord.createSocketServer(new InetSocketAddress(InetAddress.getLoopbackAddress(), 27015));
        List<ServerRecord> serverRecords = new ArrayList<>();
        serverRecords.add(record);
        serverList.replaceList(serverRecords);

        ServerRecord nextRecord = serverList.getNextServerCandidate(ProtocolTypes.TCP);
        assertEquals(record.getEndpoint(), nextRecord.getEndpoint());
        assertEquals(1, nextRecord.getProtocolTypes().size());
        assertTrue(nextRecord.getProtocolTypes().contains(ProtocolTypes.TCP));
    }

    @Test
    public void getNextServerCandidate_ReturnsServer_IfListHasServers_EvenIfAllServersAreBad() {
        serverList.getAllEndPoints();

        ServerRecord record = ServerRecord.createSocketServer(new InetSocketAddress(InetAddress.getLoopbackAddress(), 27015));
        List<ServerRecord> serverRecords = new ArrayList<>();
        serverRecords.add(record);
        serverList.replaceList(serverRecords);
        serverList.tryMark(record.getEndpoint(), record.getProtocolTypes(), ServerQuality.BAD);

        ServerRecord nextRecord = serverList.getNextServerCandidate(ProtocolTypes.TCP);
        assertEquals(record.getEndpoint(), nextRecord.getEndpoint());
        assertEquals(1, nextRecord.getProtocolTypes().size());
        assertTrue(nextRecord.getProtocolTypes().contains(ProtocolTypes.TCP));
    }

    @Test
    public void getNextServerCandidate_IsBiasedTowardsServerOrdering() {
        serverList.getAllEndPoints();

        ServerRecord goodRecord = ServerRecord.createSocketServer(new InetSocketAddress(InetAddress.getLoopbackAddress(), 27015));
        ServerRecord neutralRecord = ServerRecord.createSocketServer(new InetSocketAddress(InetAddress.getLoopbackAddress(), 27016));
        ServerRecord badRecord = ServerRecord.createSocketServer(new InetSocketAddress(InetAddress.getLoopbackAddress(), 27017));

        List<ServerRecord> serverRecords = new ArrayList<>();
        serverRecords.add(badRecord);
        serverRecords.add(neutralRecord);
        serverRecords.add(goodRecord);
        serverList.replaceList(serverRecords);

        serverList.tryMark(goodRecord.getEndpoint(), goodRecord.getProtocolTypes(), ServerQuality.GOOD);
        serverList.tryMark(badRecord.getEndpoint(), badRecord.getProtocolTypes(), ServerQuality.BAD);

        ServerRecord nextRecord = serverList.getNextServerCandidate(ProtocolTypes.TCP);
        assertEquals(neutralRecord.getEndpoint(), nextRecord.getEndpoint());
        assertEquals(1, nextRecord.getProtocolTypes().size());
        assertTrue(nextRecord.getProtocolTypes().contains(ProtocolTypes.TCP));

        serverList.tryMark(badRecord.getEndpoint(), badRecord.getProtocolTypes(), ServerQuality.GOOD);

        nextRecord = serverList.getNextServerCandidate(ProtocolTypes.TCP);
        assertEquals(badRecord.getEndpoint(), nextRecord.getEndpoint());
        assertEquals(1, nextRecord.getProtocolTypes().size());
        assertTrue(nextRecord.getProtocolTypes().contains(ProtocolTypes.TCP));
    }

    @Test
    public void getNextServerCandidate_OnlyReturnsMatchingServerOfType() {
        ServerRecord record = ServerRecord.createWebSocketServer("localhost:443");
        List<ServerRecord> serverRecords = new ArrayList<>();
        serverRecords.add(record);
        serverList.replaceList(serverRecords);

        ServerRecord endPoint = serverList.getNextServerCandidate(ProtocolTypes.TCP);
        assertNull(endPoint);
        endPoint = serverList.getNextServerCandidate(ProtocolTypes.UDP);
        assertNull(endPoint);
        endPoint = serverList.getNextServerCandidate(EnumSet.of(ProtocolTypes.TCP, ProtocolTypes.UDP));
        assertNull(endPoint);

        endPoint = serverList.getNextServerCandidate(ProtocolTypes.WEB_SOCKET);
        assertEquals(record.getEndpoint(), endPoint.getEndpoint());
        assertEquals(1, endPoint.getProtocolTypes().size());
        assertTrue(endPoint.getProtocolTypes().contains(ProtocolTypes.WEB_SOCKET));

        endPoint = serverList.getNextServerCandidate(ProtocolTypes.ALL);
        assertEquals(record.getEndpoint(), endPoint.getEndpoint());
        assertEquals(1, endPoint.getProtocolTypes().size());
        assertTrue(endPoint.getProtocolTypes().contains(ProtocolTypes.WEB_SOCKET));

        record = ServerRecord.createSocketServer(new InetSocketAddress(InetAddress.getLoopbackAddress(), 27015));
        serverRecords = new ArrayList<>();
        serverRecords.add(record);
        serverList.replaceList(serverRecords);

        endPoint = serverList.getNextServerCandidate(ProtocolTypes.WEB_SOCKET);
        assertNull(endPoint);

        endPoint = serverList.getNextServerCandidate(ProtocolTypes.TCP);
        assertEquals(record.getEndpoint(), endPoint.getEndpoint());
        assertEquals(1, endPoint.getProtocolTypes().size());
        assertTrue(endPoint.getProtocolTypes().contains(ProtocolTypes.TCP));

        endPoint = serverList.getNextServerCandidate(ProtocolTypes.UDP);
        assertEquals(record.getEndpoint(), endPoint.getEndpoint());
        assertEquals(1, endPoint.getProtocolTypes().size());
        assertTrue(endPoint.getProtocolTypes().contains(ProtocolTypes.UDP));

        endPoint = serverList.getNextServerCandidate(EnumSet.of(ProtocolTypes.TCP, ProtocolTypes.UDP));
        assertEquals(record.getEndpoint(), endPoint.getEndpoint());
        assertEquals(1, endPoint.getProtocolTypes().size());
        assertTrue(endPoint.getProtocolTypes().contains(ProtocolTypes.TCP));

        endPoint = serverList.getNextServerCandidate(ProtocolTypes.ALL);
        assertEquals(record.getEndpoint(), endPoint.getEndpoint());
        assertEquals(1, endPoint.getProtocolTypes().size());
        assertTrue(endPoint.getProtocolTypes().contains(ProtocolTypes.TCP));
    }

    @Test
    public void tryMark_ReturnsTrue_IfServerInList() {
        ServerRecord record = ServerRecord.createSocketServer(new InetSocketAddress(InetAddress.getLoopbackAddress(), 27015));
        List<ServerRecord> serverRecords = new ArrayList<>();
        serverRecords.add(record);
        serverList.replaceList(serverRecords);

        boolean marked = serverList.tryMark(record.getEndpoint(), record.getProtocolTypes(), ServerQuality.GOOD);
        assertTrue(marked);
    }

    @Test
    public void tryMark_ReturnsFalse_IfServerNotInList() {
        ServerRecord record = ServerRecord.createSocketServer(new InetSocketAddress(InetAddress.getLoopbackAddress(), 27015));
        List<ServerRecord> serverRecords = new ArrayList<>();
        serverRecords.add(record);
        serverList.replaceList(serverRecords);

        boolean marked = serverList.tryMark(new InetSocketAddress(InetAddress.getLoopbackAddress(), 27016), record.getProtocolTypes(), ServerQuality.GOOD);
        assertFalse(marked);
    }

    @Test
    public void treatsProtocolsForSameServerIndividiually() {
        ServerRecord record1 = ServerRecord.createServer(InetAddress.getLoopbackAddress().toString(), 27015, EnumSet.of(ProtocolTypes.TCP, ProtocolTypes.UDP));
        ServerRecord record2 = ServerRecord.createServer(InetAddress.getLoopbackAddress().toString(), 27016, EnumSet.of(ProtocolTypes.TCP, ProtocolTypes.UDP));
        List<ServerRecord> serverRecords = new ArrayList<>();
        serverRecords.add(record1);
        serverRecords.add(record2);
        serverList.replaceList(serverRecords);

        ServerRecord nextTcp = serverList.getNextServerCandidate(ProtocolTypes.TCP);
        ServerRecord nextUdp = serverList.getNextServerCandidate(ProtocolTypes.UDP);

        assertEquals(record1.getEndpoint(), nextTcp.getEndpoint());
        assertEquals(record1.getEndpoint(), nextUdp.getEndpoint());

        serverList.tryMark(record1.getEndpoint(), ProtocolTypes.TCP, ServerQuality.BAD);

        nextTcp = serverList.getNextServerCandidate(ProtocolTypes.TCP);
        nextUdp = serverList.getNextServerCandidate(ProtocolTypes.UDP);

        assertEquals(record2.getEndpoint(), nextTcp.getEndpoint());
        assertEquals(record1.getEndpoint(), nextUdp.getEndpoint());

        serverList.tryMark(record1.getEndpoint(), ProtocolTypes.UDP, ServerQuality.BAD);

        nextTcp = serverList.getNextServerCandidate(ProtocolTypes.TCP);
        nextUdp = serverList.getNextServerCandidate(ProtocolTypes.UDP);

        assertEquals(record2.getEndpoint(), nextTcp.getEndpoint());
        assertEquals(record2.getEndpoint(), nextUdp.getEndpoint());
    }
}