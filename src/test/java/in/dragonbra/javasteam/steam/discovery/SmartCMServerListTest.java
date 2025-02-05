package in.dragonbra.javasteam.steam.discovery;

import in.dragonbra.javasteam.TestBase;
import in.dragonbra.javasteam.networking.steam3.ProtocolTypes;
import in.dragonbra.javasteam.steam.steamclient.configuration.SteamConfiguration;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author lngtr
 * @since 2018-02-25
 */
public class SmartCMServerListTest extends TestBase {

    private SmartCMServerList serverList;

    @BeforeEach
    public void setUp() {
        var configuration = SteamConfiguration.create(b -> b.withDirectoryFetch(false));
        serverList = new SmartCMServerList(configuration);
    }

    @Test
    public void tryMergeWithList_AddsToHead_AndMovesExisting() {
        serverList.getAllEndPoints();

        var seedList = List.of(
                ServerRecord.createSocketServer(new InetSocketAddress(InetAddress.getLoopbackAddress(), 27025)),
                ServerRecord.createSocketServer(new InetSocketAddress(InetAddress.getLoopbackAddress(), 27035)),
                ServerRecord.createSocketServer(new InetSocketAddress(InetAddress.getLoopbackAddress(), 27045)),
                ServerRecord.createSocketServer(new InetSocketAddress(InetAddress.getLoopbackAddress(), 27105))
        );
        serverList.replaceList(seedList);
        Assertions.assertEquals(4, seedList.size());

        var listToReplace = List.of(
                ServerRecord.createSocketServer(new InetSocketAddress(InetAddress.getLoopbackAddress(), 27015)),
                ServerRecord.createSocketServer(new InetSocketAddress(InetAddress.getLoopbackAddress(), 27035)),
                ServerRecord.createSocketServer(new InetSocketAddress(InetAddress.getLoopbackAddress(), 27105))
        );
        serverList.replaceList(listToReplace);

        var addresses = serverList.getAllEndPoints();
        Assertions.assertEquals(3, addresses.size());
        Assertions.assertEquals(listToReplace.get(0), addresses.get(0));
        Assertions.assertEquals(listToReplace.get(1), addresses.get(1));
        Assertions.assertEquals(listToReplace.get(2), addresses.get(2));
    }

    @Test
    public void getNextServerCandidate_ReturnsNull_IfListIsEmpty() {
        var endPoint = serverList.getNextServerCandidate(ProtocolTypes.TCP);
        Assertions.assertNull(endPoint);
    }

    @Test
    public void getNextServerCandidate_ReturnsServer_IfListHasServers() {
        serverList.getAllEndPoints();

        var record = ServerRecord.createSocketServer(new InetSocketAddress(InetAddress.getLoopbackAddress(), 27015));
        serverList.replaceList(List.of(record));

        var nextRecord = serverList.getNextServerCandidate(ProtocolTypes.TCP);
        Assertions.assertEquals(record.getEndpoint(), nextRecord.getEndpoint());
        Assertions.assertTrue(nextRecord.getProtocolTypes().contains(ProtocolTypes.TCP));

        Assertions.assertEquals(1, nextRecord.getProtocolTypes().size());
    }

    @Test
    public void getNextServerCandidate_ReturnsServer_IfListHasServers_EvenIfAllServersAreBad() {
        serverList.getAllEndPoints();

        var record = ServerRecord.createSocketServer(new InetSocketAddress(InetAddress.getLoopbackAddress(), 27015));
        serverList.replaceList(List.of(record));
        serverList.tryMark(record.getEndpoint(), record.getProtocolTypes(), ServerQuality.BAD);

        var nextRecord = serverList.getNextServerCandidate(ProtocolTypes.TCP);
        Assertions.assertEquals(record.getEndpoint(), nextRecord.getEndpoint());
        Assertions.assertTrue(nextRecord.getProtocolTypes().contains(ProtocolTypes.TCP));

        Assertions.assertEquals(1, nextRecord.getProtocolTypes().size());
    }

    @Test
    public void getNextServerCandidate_IsBiasedTowardsServerOrdering() throws UnknownHostException {
        serverList.getAllEndPoints();

        var serverA = InetAddress.getByName("10.0.0.1");
        var serverB = InetAddress.getByName("10.0.0.2");

        var goodRecord = ServerRecord.createSocketServer(new InetSocketAddress(serverA, 27015));
        var neutralRecord = ServerRecord.createSocketServer(new InetSocketAddress(serverA, 27016));
        var badRecord = ServerRecord.createSocketServer(new InetSocketAddress(serverA, 27017));
        var serverBRecord = ServerRecord.createSocketServer(new InetSocketAddress(serverB, 27017));

        serverList.replaceList(List.of(badRecord, neutralRecord, goodRecord, serverBRecord));

        serverList.tryMark(badRecord.getEndpoint(), badRecord.getProtocolTypes(), ServerQuality.BAD);
        serverList.tryMark(goodRecord.getEndpoint(), goodRecord.getProtocolTypes(), ServerQuality.GOOD);

        // Server A's endpoints were all marked bad, with goodRecord being recovered
        var nextRecord = serverList.getNextServerCandidate(ProtocolTypes.TCP);
        Assertions.assertEquals(goodRecord.getEndpoint(), nextRecord.getEndpoint());
        Assertions.assertTrue(nextRecord.getProtocolTypes().contains(ProtocolTypes.TCP));

        Assertions.assertEquals(1, nextRecord.getProtocolTypes().size());

        serverList.tryMark(badRecord.getEndpoint(), badRecord.getProtocolTypes(), ServerQuality.GOOD);

        // Server A's bad record is now at the front, having been marked good
        nextRecord = serverList.getNextServerCandidate(ProtocolTypes.TCP);
        Assertions.assertEquals(badRecord.getEndpoint(), nextRecord.getEndpoint());
        Assertions.assertTrue(nextRecord.getProtocolTypes().contains(ProtocolTypes.TCP));

        Assertions.assertEquals(1, nextRecord.getProtocolTypes().size());
    }

    @Test
    public void getNextServerCandidate_AllEndpointsByHostAreBad() throws UnknownHostException {
        serverList.getAllEndPoints();

        var serverA = InetAddress.getByName("10.0.0.1");
        var serverB = InetAddress.getByName("10.0.0.2");

        var goodRecord = ServerRecord.createSocketServer(new InetSocketAddress(serverA, 27015));
        var neutralRecord = ServerRecord.createSocketServer(new InetSocketAddress(serverA, 27016));
        var badRecord = ServerRecord.createSocketServer(new InetSocketAddress(serverA, 27017));
        var serverBRecord = ServerRecord.createSocketServer(new InetSocketAddress(serverB, 27017));

        serverList.replaceList(List.of(goodRecord, neutralRecord, badRecord, serverBRecord));

        serverList.tryMark(goodRecord.getEndpoint(), goodRecord.getProtocolTypes(), ServerQuality.GOOD);
        serverList.tryMark(badRecord.getEndpoint(), badRecord.getProtocolTypes(), ServerQuality.BAD);

        // Server A's endpoints are all bad. Server B is our next candidate.
        var nextRecord = serverList.getNextServerCandidate(ProtocolTypes.TCP);
        Assertions.assertEquals(serverBRecord.getEndpoint(), nextRecord.getEndpoint());
        Assertions.assertTrue(nextRecord.getProtocolTypes().contains(ProtocolTypes.TCP));
    }

    @Test
    public void getNextServerCandidate_OnlyReturnsMatchingServerOfType() {
        var record = ServerRecord.createWebSocketServer("localhost:443");
        serverList.replaceList(List.of(record));

        var endPoint = serverList.getNextServerCandidate(ProtocolTypes.TCP);
        Assertions.assertNull(endPoint);
        endPoint = serverList.getNextServerCandidate(ProtocolTypes.UDP);
        Assertions.assertNull(endPoint);
        endPoint = serverList.getNextServerCandidate(EnumSet.of(ProtocolTypes.TCP, ProtocolTypes.UDP));
        Assertions.assertNull(endPoint);

        endPoint = serverList.getNextServerCandidate(ProtocolTypes.WEB_SOCKET);
        Assertions.assertEquals(record.getEndpoint(), endPoint.getEndpoint());
        Assertions.assertTrue(endPoint.getProtocolTypes().contains(ProtocolTypes.WEB_SOCKET));

        Assertions.assertEquals(1, endPoint.getProtocolTypes().size());

        endPoint = serverList.getNextServerCandidate(ProtocolTypes.ALL);
        Assertions.assertEquals(record.getEndpoint(), endPoint.getEndpoint());
        Assertions.assertTrue(endPoint.getProtocolTypes().contains(ProtocolTypes.WEB_SOCKET));

        Assertions.assertEquals(1, endPoint.getProtocolTypes().size());

        record = ServerRecord.createSocketServer(new InetSocketAddress(InetAddress.getLoopbackAddress(), 27015));
        serverList.replaceList(List.of(record));

        endPoint = serverList.getNextServerCandidate(ProtocolTypes.WEB_SOCKET);
        Assertions.assertNull(endPoint);

        endPoint = serverList.getNextServerCandidate(ProtocolTypes.TCP);
        Assertions.assertEquals(record.getEndpoint(), endPoint.getEndpoint());
        Assertions.assertTrue(endPoint.getProtocolTypes().contains(ProtocolTypes.TCP));

        Assertions.assertEquals(1, endPoint.getProtocolTypes().size());

        endPoint = serverList.getNextServerCandidate(ProtocolTypes.UDP);
        Assertions.assertEquals(record.getEndpoint(), endPoint.getEndpoint());
        Assertions.assertTrue(endPoint.getProtocolTypes().contains(ProtocolTypes.UDP));

        Assertions.assertEquals(1, endPoint.getProtocolTypes().size());

        endPoint = serverList.getNextServerCandidate(EnumSet.of(ProtocolTypes.TCP, ProtocolTypes.UDP));
        Assertions.assertEquals(record.getEndpoint(), endPoint.getEndpoint());
        Assertions.assertTrue(endPoint.getProtocolTypes().contains(ProtocolTypes.TCP));

        Assertions.assertEquals(1, endPoint.getProtocolTypes().size());

        endPoint = serverList.getNextServerCandidate(ProtocolTypes.ALL);
        Assertions.assertEquals(record.getEndpoint(), endPoint.getEndpoint());
        Assertions.assertTrue(endPoint.getProtocolTypes().contains(ProtocolTypes.TCP));

        Assertions.assertEquals(1, endPoint.getProtocolTypes().size());
    }

    @Test
    public void getNextServerCandidate_MarkIterateAllCandidates() {
        serverList.getAllEndPoints();

        var recordA = ServerRecord.createWebSocketServer("10.0.0.1:27030");
        var recordB = ServerRecord.createWebSocketServer("10.0.0.2:27030");
        var recordC = ServerRecord.createWebSocketServer("10.0.0.3:27030");

        // Add all candidates
        serverList.replaceList(List.of(recordA, recordB, recordC));

        var candidatesReturned = new HashSet<ServerRecord>();

        Runnable dequeueAndMarkCandidate = () -> {
            ServerRecord candidate = serverList.getNextServerCandidate(ProtocolTypes.WEB_SOCKET);
            Assertions.assertTrue(candidatesReturned.add(candidate), "Candidate " + candidate.getEndpoint() + " already seen");
            try {
                TimeUnit.MILLISECONDS.sleep(10);
            } catch (InterruptedException e) {
                Assertions.fail(e);
            }
            serverList.tryMark(candidate.getEndpoint(), ProtocolTypes.WEB_SOCKET, ServerQuality.BAD);
        };

        // We must dequeue all servers as they all get marked bad
        dequeueAndMarkCandidate.run();
        dequeueAndMarkCandidate.run();
        dequeueAndMarkCandidate.run();
        Assertions.assertEquals(3, candidatesReturned.size(), "All candidates returned");
    }

    @Test
    public void getNextServerCandidate_MarkIterateAllBadCandidates() {
        serverList.getAllEndPoints();

        var recordA = ServerRecord.createWebSocketServer("10.0.0.1:27030");
        var recordB = ServerRecord.createWebSocketServer("10.0.0.2:27030");
        var recordC = ServerRecord.createWebSocketServer("10.0.0.3:27030");

        // Add all candidates and mark them bad
        serverList.replaceList(List.of(recordA, recordB, recordC));
        serverList.tryMark(recordA.getEndpoint(), ProtocolTypes.WEB_SOCKET, ServerQuality.BAD);
        serverList.tryMark(recordB.getEndpoint(), ProtocolTypes.WEB_SOCKET, ServerQuality.BAD);
        serverList.tryMark(recordC.getEndpoint(), ProtocolTypes.WEB_SOCKET, ServerQuality.BAD);

        var candidatesReturned = new HashSet<ServerRecord>();

        Runnable dequeueAndMarkCandidate = () -> {
            var candidate = serverList.getNextServerCandidate(ProtocolTypes.WEB_SOCKET);
            Assertions.assertTrue(candidatesReturned.add(candidate), "Candidate " + candidate.getEndpoint() + " already seen");
            try {
                TimeUnit.MILLISECONDS.sleep(10);
            } catch (InterruptedException e) {
                Assertions.fail(e);
            }
            serverList.tryMark(candidate.getEndpoint(), ProtocolTypes.WEB_SOCKET, ServerQuality.BAD);
        };

        // We must dequeue all candidates from a bad list
        dequeueAndMarkCandidate.run();
        dequeueAndMarkCandidate.run();
        dequeueAndMarkCandidate.run();
        Assertions.assertEquals(3, candidatesReturned.size(), "All candidates returned");
    }

    @Test
    public void tryMark_ReturnsTrue_IfServerInList() {
        var record = ServerRecord.createSocketServer(new InetSocketAddress(InetAddress.getLoopbackAddress(), 27015));
        serverList.replaceList(List.of(record));

        var marked = serverList.tryMark(record.getEndpoint(), record.getProtocolTypes(), ServerQuality.GOOD);
        Assertions.assertTrue(marked);
    }

    @Test
    public void tryMark_ReturnsFalse_IfServerNotInList() {
        var record = ServerRecord.createSocketServer(new InetSocketAddress(InetAddress.getLoopbackAddress(), 27015));
        serverList.replaceList(List.of(record));

        var marked = serverList.tryMark(new InetSocketAddress(InetAddress.getLoopbackAddress(), 27016), record.getProtocolTypes(), ServerQuality.GOOD);
        Assertions.assertFalse(marked);
    }

    @Test
    public void testNullConnection_ShouldReturnFalse() {
        var result = serverList.tryMark(null, ProtocolTypes.WEB_SOCKET, ServerQuality.BAD);

        Assertions.assertFalse(result);
    }
}
