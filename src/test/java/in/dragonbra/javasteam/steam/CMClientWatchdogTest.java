package in.dragonbra.javasteam.steam;

import in.dragonbra.javasteam.TestBase;
import in.dragonbra.javasteam.base.ClientMsgProtobuf;
import in.dragonbra.javasteam.enums.EMsg;
import in.dragonbra.javasteam.enums.EResult;
import in.dragonbra.javasteam.networking.steam3.Connection;
import in.dragonbra.javasteam.networking.steam3.NetMsgEventArgs;
import in.dragonbra.javasteam.networking.steam3.ProtocolTypes;
import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserverLogin.CMsgClientHeartBeat;
import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserverLogin.CMsgClientLoggedOff;
import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserverLogin.CMsgClientLogonResponse;
import in.dragonbra.javasteam.steam.discovery.ServerRecord;
import in.dragonbra.javasteam.steam.steamclient.configuration.SteamConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * CMClient's watchdog, which disconnects a logged on connection that stops receiving anything.
 * Steam does this during maintenance: no close, no logoff, and sends still succeed.
 */
public class CMClientWatchdogTest extends TestBase {

    private static final long WATCHDOG_TIMEOUT_MS = 500L;

    // Generous wait for an event that should happen; long enough to prove one doesn't.
    private static final long EXPECT_MS = 5_000L;

    private static final long QUIET_MS = WATCHDOG_TIMEOUT_MS * 3;

    private final FakeConnection connection = new FakeConnection();

    private TestClient client;

    @AfterEach
    void tearDown() {
        if (client != null) {
            client.disconnect();
        }
    }

    @Test
    void silenceAfterLogonDisconnects() throws Exception {
        client = connectedClient(WATCHDOG_TIMEOUT_MS);

        connection.receive(logonResponse());

        // Not user initiated, so apps reconnect
        assertEquals(Boolean.FALSE, client.disconnects.poll(EXPECT_MS, TimeUnit.MILLISECONDS));
        assertEquals(Boolean.FALSE, connection.disconnects.poll());
    }

    @Test
    void trafficKeepsTheWatchdogAway() throws Exception {
        client = connectedClient(WATCHDOG_TIMEOUT_MS);

        connection.receive(logonResponse());

        // Like heartbeat replies: a message every 100ms, well inside the 500ms watchdog
        ScheduledExecutorService replies = Executors.newSingleThreadScheduledExecutor();
        try {
            replies.scheduleAtFixedRate(() -> connection.receive(heartbeat()), 0, 100, TimeUnit.MILLISECONDS);

            assertNull(client.disconnects.poll(QUIET_MS, TimeUnit.MILLISECONDS), "watchdog fired despite steady traffic");
        } finally {
            replies.shutdownNow();
        }
    }

    @Test
    void silenceBeforeLogonIsAllowed() throws Exception {
        // Connected but not logged on yet, e.g. waiting on the user for a Steam Guard code: nothing guarantees traffic
        client = connectedClient(WATCHDOG_TIMEOUT_MS);

        assertNull(client.disconnects.poll(QUIET_MS, TimeUnit.MILLISECONDS), "watchdog fired before logon");
    }

    @Test
    void loggingOffStopsTheWatchdog() throws Exception {
        // Logged off, but the CM keeps the socket open
        client = connectedClient(WATCHDOG_TIMEOUT_MS);

        connection.receive(logonResponse());
        connection.receive(loggedOff());

        assertNull(client.disconnects.poll(QUIET_MS, TimeUnit.MILLISECONDS), "watchdog fired after logging off");
    }

    @Test
    void zeroTimeoutDisablesTheWatchdog() throws Exception {
        client = connectedClient(0L);

        connection.receive(logonResponse());

        assertNull(client.disconnects.poll(QUIET_MS, TimeUnit.MILLISECONDS), "watchdog fired while disabled");
    }

    @Test
    void reconnectingRestartsTheWatchdog() throws Exception {
        client = connectedClient(WATCHDOG_TIMEOUT_MS);

        connection.receive(logonResponse());
        assertEquals(Boolean.FALSE, client.disconnects.poll(EXPECT_MS, TimeUnit.MILLISECONDS));

        // The watchdog is off again until the next logon
        client.connect(server());
        connection.raiseConnected();
        assertNull(client.disconnects.poll(QUIET_MS, TimeUnit.MILLISECONDS), "watchdog fired before the new logon");

        connection.receive(logonResponse());
        assertEquals(Boolean.FALSE, client.disconnects.poll(EXPECT_MS, TimeUnit.MILLISECONDS));
    }

    // region Helpers

    private TestClient connectedClient(long watchdogTimeout) {
        var configuration = SteamConfiguration.create(builder -> builder
                .withConnectionFactory((config, protocol) -> connection)
                .withWatchdogTimeout(watchdogTimeout));

        var testClient = new TestClient(configuration);
        testClient.connect(server());
        connection.raiseConnected();

        return testClient;
    }

    private static ServerRecord server() {
        return ServerRecord.createServer("127.0.0.1", 443, ProtocolTypes.WEB_SOCKET);
    }

    private static byte[] logonResponse() {
        var msg = new ClientMsgProtobuf<CMsgClientLogonResponse.Builder>(CMsgClientLogonResponse.class, EMsg.ClientLogOnResponse);
        msg.getBody().setEresult(EResult.OK.code());
        msg.getBody().setLegacyOutOfGameHeartbeatSeconds(9);
        return msg.serialize();
    }

    private static byte[] loggedOff() {
        var msg = new ClientMsgProtobuf<CMsgClientLoggedOff.Builder>(CMsgClientLoggedOff.class, EMsg.ClientLoggedOff);
        msg.getBody().setEresult(EResult.LoggedInElsewhere.code());
        return msg.serialize();
    }

    private static byte[] heartbeat() {
        return new ClientMsgProtobuf<CMsgClientHeartBeat.Builder>(CMsgClientHeartBeat.class, EMsg.ClientHeartBeat).serialize();
    }

    private static final class TestClient extends CMClient {

        final BlockingQueue<Boolean> disconnects = new LinkedBlockingQueue<>();

        TestClient(SteamConfiguration configuration) {
            super(configuration);
        }

        @Override
        protected void onClientDisconnected(boolean userInitiated) {
            super.onClientDisconnected(userInitiated);
            disconnects.add(userInitiated);
        }
    }

    /**
     * Stands in for the transport: CMClient's sends go nowhere, and like the real connections it raises
     * Disconnected before disconnect() returns.
     */
    private static final class FakeConnection extends Connection {

        private static final InetSocketAddress FAKE_END_POINT = new InetSocketAddress(InetAddress.getLoopbackAddress(), 443);

        final BlockingQueue<Boolean> disconnects = new LinkedBlockingQueue<>();

        @Override
        public void connect(@NotNull InetSocketAddress endPoint, int timeout) {
        }

        @Override
        public void disconnect(boolean userInitiated) {
            disconnects.add(userInitiated);
            onDisconnected(userInitiated);
        }

        @Override
        public void send(byte @NotNull [] data) {
        }

        @Override
        public @Nullable InetAddress getLocalIP() {
            return InetAddress.getLoopbackAddress();
        }

        @Override
        public @Nullable InetSocketAddress getCurrentEndPoint() {
            return FAKE_END_POINT;
        }

        @Override
        public @Nullable ProtocolTypes getProtocolTypes() {
            return ProtocolTypes.WEB_SOCKET;
        }

        void raiseConnected() {
            onConnected();
        }

        void receive(byte[] data) {
            onNetMsgReceived(new NetMsgEventArgs(data, FAKE_END_POINT));
        }
    }

    // endregion
}
