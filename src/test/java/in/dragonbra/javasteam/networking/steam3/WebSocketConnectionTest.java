package in.dragonbra.javasteam.networking.steam3;

import mockwebserver3.MockResponse;
import mockwebserver3.MockWebServer;
import mockwebserver3.SocketEffect;
import okhttp3.OkHttpClient;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okhttp3.tls.HandshakeCertificates;
import okhttp3.tls.HeldCertificate;
import okio.ByteString;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Plays out connection and failure conditions against a local TLS {@link MockWebServer},
 * standing in for a Steam CM over wss://.
 * Includes the cases from SteamKit2 WebSocketConnectionFacts (SK PR 1657).
 */
public class WebSocketConnectionTest {

    private static final int CONNECT_TIMEOUT_MS = 500;

    // Generous wait for an event that should happen; short wait to prove one doesn't.
    private static final long EXPECT_MS = 5_000L;

    private static final long QUIET_MS = 750L;

    private static HandshakeCertificates serverCertificates;

    private static OkHttpClient trustingClient;

    private MockWebServer server;

    @BeforeAll
    static void setUpCertificates() {
        var localhost = new HeldCertificate.Builder()
                .addSubjectAlternativeName("localhost")
                .addSubjectAlternativeName("::1")
                .build();

        serverCertificates = new HandshakeCertificates.Builder()
                .heldCertificate(localhost)
                .build();

        var clientCertificates = new HandshakeCertificates.Builder()
                .addTrustedCertificate(localhost.certificate())
                .build();

        trustingClient = new OkHttpClient.Builder()
                .sslSocketFactory(clientCertificates.sslSocketFactory(), clientCertificates.trustManager())
                .build();
    }

    @BeforeEach
    void setUp() throws IOException {
        server = new MockWebServer();
        server.useHttps(serverCertificates.sslSocketFactory());
        server.start();
    }

    @AfterEach
    void tearDown() {
        server.close();
    }

    // region Happy path

    @Test
    void connectsAndReceivesMessagesInOrder() throws Exception {
        var cm = new FakeCm().echo();
        server.enqueue(upgrade(cm));

        var connection = newConnection();
        var events = new Events(connection);
        connection.connect(endpoint(), CONNECT_TIMEOUT_MS);

        events.expect("connected");
        assertEquals("/cmsocket/", server.takeRequest().getUrl().encodedPath());
        assertEquals(endpoint(), connection.getCurrentEndPoint());

        cm.awaitOpen().send(ByteString.of((byte) 1, (byte) 2, (byte) 3));
        assertArrayEquals(new byte[]{1, 2, 3}, events.expectMessage());

        // Echoed back in the order they were sent
        for (byte i = 0; i < 10; i++) {
            connection.send(new byte[]{i});
        }
        for (byte i = 0; i < 10; i++) {
            assertArrayEquals(new byte[]{i}, events.expectMessage());
        }

        connection.disconnect(true);
    }

    @Test
    void largeMessageArrivesOnce() throws Exception {
        var cm = new FakeCm();
        server.enqueue(upgrade(cm));

        var connection = newConnection();
        var events = new Events(connection);
        connection.connect(endpoint(), CONNECT_TIMEOUT_MS);
        events.expect("connected");

        var payload = new byte[1024 * 1024];
        for (int i = 0; i < payload.length; i++) {
            payload[i] = (byte) i;
        }
        cm.awaitOpen().send(ByteString.of(payload));

        assertArrayEquals(payload, events.expectMessage());
        events.expectQuiet();

        connection.disconnect(true);
    }

    @Test
    void sendDeliversASingleBinaryMessage() throws Exception {
        var cm = new FakeCm();
        server.enqueue(upgrade(cm));

        var connection = newConnection();
        var events = new Events(connection);
        connection.connect(endpoint(), CONNECT_TIMEOUT_MS);
        events.expect("connected");
        cm.awaitOpen();

        connection.send(new byte[]{9, 8, 7, 6});

        assertArrayEquals(new byte[]{9, 8, 7, 6}, cm.expectMessage());
        assertNull(cm.received.poll(QUIET_MS, TimeUnit.MILLISECONDS), "unexpected extra message");

        connection.disconnect(true);
    }

    @Test
    void textAndEmptyMessagesAreIgnored() throws Exception {
        var cm = new FakeCm();
        server.enqueue(upgrade(cm));

        var connection = newConnection();
        var events = new Events(connection);
        connection.connect(endpoint(), CONNECT_TIMEOUT_MS);
        events.expect("connected");

        var serverSocket = cm.awaitOpen();
        serverSocket.send("hello");
        serverSocket.send(ByteString.EMPTY);
        serverSocket.send(ByteString.of((byte) 1));

        // Only the binary message with data surfaces, and the socket stays up
        assertArrayEquals(new byte[]{1}, events.expectMessage());
        events.expectQuiet();

        connection.disconnect(true);
    }

    @Test
    void idleConnectionStaysUp() throws Exception {
        // Connected but not logged on yet, e.g. waiting on the user for a Steam Guard code: nothing guarantees
        // traffic, so silence alone must not drop the socket. CMClient's watchdog handles silence after logon.
        server.enqueue(upgrade(new FakeCm()));

        var connection = newConnection();
        var events = new Events(connection);
        connection.connect(endpoint(), CONNECT_TIMEOUT_MS);
        events.expect("connected");

        Thread.sleep(QUIET_MS * 2);

        events.expectQuiet();

        connection.disconnect(true);
    }

    @Test
    void connectsToAnIpv6Address() throws Exception {
        var loopback = InetAddress.getByName("::1");

        var ipv6Server = new MockWebServer();

        try (ipv6Server) {
            ipv6Server.useHttps(serverCertificates.sslSocketFactory());
            try {
                ipv6Server.start(loopback, 0);
            } catch (IOException e) {
                Assumptions.abort("IPv6 loopback unavailable: " + e.getMessage());
            }
            ipv6Server.enqueue(upgrade(new FakeCm()));

            var connection = newConnection();
            var events = new Events(connection);
            connection.connect(new InetSocketAddress(loopback, ipv6Server.getPort()), CONNECT_TIMEOUT_MS);

            // A raw "wss://::1:port" URL is rejected outright; the host needs brackets
            events.expect("connected");
            assertEquals("/cmsocket/", ipv6Server.takeRequest().getUrl().encodedPath());

            connection.disconnect(true);
        }
    }

    // endregion

    // region Connect failures (maintenance: CMs accept TCP but misbehave)

    @Test
    void stalledHandshakeTimesOut() throws Exception {
        // TLS completes, then the server never reads the upgrade request or answers it
        server.enqueue(new MockResponse.Builder().onRequestStart(SocketEffect.Stall.INSTANCE).build());

        var connection = newConnection();
        var events = new Events(connection);
        connection.connect(endpoint(), CONNECT_TIMEOUT_MS);

        events.expect("disconnected:false");
        events.expectQuiet();
    }

    @Test
    void rejectedUpgradeDisconnects() throws Exception {
        server.enqueue(new MockResponse.Builder().code(503).build());

        var connection = newConnection();
        var events = new Events(connection);
        connection.connect(endpoint(), CONNECT_TIMEOUT_MS);

        events.expect("disconnected:false");
        events.expectQuiet();
    }

    @Test
    void failedTlsHandshakeDisconnects() throws Exception {
        server.enqueue(new MockResponse.Builder().failHandshake().build());

        var connection = newConnection();
        var events = new Events(connection);
        connection.connect(endpoint(), CONNECT_TIMEOUT_MS);

        events.expect("disconnected:false");
        events.expectQuiet();
    }

    @Test
    void nothingListeningDisconnects() throws Exception {
        var port = server.getPort();
        server.close();

        var connection = newConnection();
        var events = new Events(connection);
        connection.connect(new InetSocketAddress("localhost", port), CONNECT_TIMEOUT_MS);

        events.expect("disconnected:false");
    }

    // endregion

    // region Failures after connecting

    @Test
    void serverCloseIsNotUserInitiated() throws Exception {
        var cm = new FakeCm();
        server.enqueue(upgrade(cm));

        var connection = newConnection();
        var events = new Events(connection);
        connection.connect(endpoint(), CONNECT_TIMEOUT_MS);
        events.expect("connected");

        cm.awaitOpen().close(1000, "bye");

        // Not user initiated, so apps reconnect
        events.expect("disconnected:false");
        events.expectQuiet();

        // The close frame is answered
        assertEquals(1000, cm.awaitClosed());

        // CMClient reads the endpoint inside the Disconnected handler to mark the server bad,
        // and it is cleared once the handler has run
        assertEquals(endpoint(), events.endpointOnDisconnect.get());
        assertNull(connection.getCurrentEndPoint());
    }

    @Test
    void abruptDropIsNotUserInitiated() throws Exception {
        var cm = new FakeCm();
        server.enqueue(upgrade(cm));

        var connection = newConnection();
        var events = new Events(connection);
        connection.connect(endpoint(), CONNECT_TIMEOUT_MS);
        events.expect("connected");

        // Closes every socket outright, with no close frame: like a CM process dying
        cm.awaitOpen();
        server.close();

        events.expect("disconnected:false");
        events.expectQuiet();
    }

    // endregion

    // region Throwing handlers (SK PR 1657)

    @Test
    void throwingNetMsgReceivedHandlerKeepsReading() throws Exception {
        var cm = new FakeCm();
        server.enqueue(upgrade(cm));

        var connection = newConnection();
        var events = new Events(connection);
        var calls = new AtomicInteger();
        connection.getNetMsgReceived().addEventHandler((sender, e) -> {
            if (calls.getAndIncrement() == 0) {
                throw new IllegalStateException("handler failure");
            }
        });

        connection.connect(endpoint(), CONNECT_TIMEOUT_MS);
        events.expect("connected");

        var serverSocket = cm.awaitOpen();
        serverSocket.send(ByteString.of((byte) 1));
        serverSocket.send(ByteString.of((byte) 2));

        // The first message reached the recorder before the throwing handler, and the read loop kept going
        assertArrayEquals(new byte[]{1}, events.expectMessage());
        assertArrayEquals(new byte[]{2}, events.expectMessage());
        events.expectQuiet();

        connection.disconnect(true);
    }

    @Test
    void throwingConnectedHandlerDisconnects() throws Exception {
        var cm = new FakeCm();
        server.enqueue(upgrade(cm));

        var connection = newConnection();
        var events = new Events(connection);
        connection.getConnected().addEventHandler((sender, e) -> {
            throw new IllegalStateException("handler failure");
        });

        connection.connect(endpoint(), CONNECT_TIMEOUT_MS);

        events.expect("connected");
        events.expect("disconnected:false");
        events.expectQuiet();
        assertEquals(1000, cm.awaitClosed());
    }

    @Test
    void disconnectFromConnectedHandlerFiresOnce() throws Exception {
        server.enqueue(upgrade(new FakeCm()));

        var connection = newConnection();
        var events = new Events(connection);
        connection.getConnected().addEventHandler((sender, e) -> connection.disconnect(true));

        connection.connect(endpoint(), CONNECT_TIMEOUT_MS);

        events.expect("connected");
        events.expect("disconnected:true");
        events.expectQuiet();
    }

    @Test
    void disconnectWhileDispatchingAMessageFiresOnce() throws Exception {
        var cm = new FakeCm();
        server.enqueue(upgrade(cm));

        var connection = newConnection();
        var events = new Events(connection);
        connection.getNetMsgReceived().addEventHandler((sender, e) -> connection.disconnect(false));

        connection.connect(endpoint(), CONNECT_TIMEOUT_MS);
        events.expect("connected");

        cm.awaitOpen().send(ByteString.of((byte) 1));

        events.expectMessage();
        events.expect("disconnected:false");
        events.expectQuiet();
    }

    // endregion

    // region Disconnect contract

    @Test
    void userDisconnectIsSynchronousAndClosesTheServer() throws Exception {
        var cm = new FakeCm();
        server.enqueue(upgrade(cm));

        var connection = newConnection();
        var events = new Events(connection);
        connection.connect(endpoint(), CONNECT_TIMEOUT_MS);
        events.expect("connected");
        cm.awaitOpen();

        connection.disconnect(true);

        // The disconnected event has fired before disconnect() returns
        assertEquals("disconnected:true", events.log.poll());
        events.expectQuiet();

        // A normal close frame reaches the server
        assertEquals(1000, cm.awaitClosed());
    }

    @Test
    void disconnectWhenNeverConnectedDoesNothing() throws Exception {
        var connection = newConnection();
        var events = new Events(connection);

        connection.disconnect(true);
        connection.disconnect(false);

        events.expectQuiet();
    }

    @Test
    void competingDisconnectsFireOnce() throws Exception {
        var cm = new FakeCm();
        server.enqueue(upgrade(cm));

        var connection = newConnection();
        var events = new Events(connection);
        connection.connect(endpoint(), CONNECT_TIMEOUT_MS);
        events.expect("connected");

        // Server close and several local disconnects, all racing
        var start = new CountDownLatch(1);
        var threads = new Thread[4];
        for (int i = 0; i < threads.length; i++) {
            threads[i] = new Thread(() -> {
                try {
                    start.await();
                    connection.disconnect(false);
                } catch (InterruptedException ignored) {
                }
            });
            threads[i].start();
        }

        cm.awaitOpen().close(1000, null);
        start.countDown();

        for (var thread : threads) {
            thread.join();
        }

        Thread.sleep(QUIET_MS);

        assertEquals(1, events.disconnects.get(), "disconnected should fire exactly once");
    }

    @Test
    void reconnectRetiresTheOldSocket() throws Exception {
        var first = new FakeCm();
        var second = new FakeCm();
        server.enqueue(upgrade(first));
        server.enqueue(upgrade(second));

        var connection = newConnection();
        var events = new Events(connection);

        connection.connect(endpoint(), CONNECT_TIMEOUT_MS);
        events.expect("connected");
        var firstSocket = first.awaitOpen();

        // Connect again on the same instance without disconnecting first: like SteamKit, the old one is
        // reported disconnected and closed
        connection.connect(endpoint(), CONNECT_TIMEOUT_MS);
        events.expect("disconnected:false");
        events.expect("connected");
        assertEquals(1000, first.awaitClosed());

        // Late activity from the replaced socket must not surface
        firstSocket.send(ByteString.of((byte) 9));
        events.expectQuiet();

        // Sends reach the new server
        second.awaitOpen();
        connection.send(new byte[]{9, 8, 7, 6});
        assertArrayEquals(new byte[]{9, 8, 7, 6}, second.expectMessage());
        assertNull(first.received.poll(), "send reached the replaced server");

        connection.disconnect(true);
        events.expect("disconnected:true");
    }

    @Test
    void sendWhileNotConnectedIsIgnored() throws Exception {
        var connection = newConnection();
        var events = new Events(connection);

        assertDoesNotThrow(() -> connection.send(new byte[]{1, 2, 3}));

        events.expectQuiet();
    }

    @Test
    void sendAfterDisconnectIsIgnored() throws Exception {
        server.enqueue(upgrade(new FakeCm()));

        var connection = newConnection();
        var events = new Events(connection);
        connection.connect(endpoint(), CONNECT_TIMEOUT_MS);
        events.expect("connected");

        connection.disconnect(true);
        events.expect("disconnected:true");

        assertDoesNotThrow(() -> connection.send(new byte[]{1, 2, 3}));
        events.expectQuiet();
    }

    // endregion

    // region Helpers

    private WebSocketConnection newConnection() {
        return new WebSocketConnection(trustingClient);
    }

    private InetSocketAddress endpoint() {
        return new InetSocketAddress("localhost", server.getPort());
    }

    private static MockResponse upgrade(FakeCm cm) {
        return new MockResponse.Builder().webSocketUpgrade(cm).build();
    }

    /**
     * The server side of the socket. By default it accepts and stays silent; tests drive it from there.
     */
    private static final class FakeCm extends WebSocketListener {

        private final CompletableFuture<WebSocket> open = new CompletableFuture<>();

        private final CompletableFuture<Integer> closed = new CompletableFuture<>();

        final BlockingQueue<byte[]> received = new LinkedBlockingQueue<>();

        private boolean echo;

        FakeCm echo() {
            echo = true;
            return this;
        }

        WebSocket awaitOpen() throws Exception {
            return open.get(EXPECT_MS, TimeUnit.MILLISECONDS);
        }

        /**
         * @return the close code the client sent.
         */
        int awaitClosed() throws Exception {
            return closed.get(EXPECT_MS, TimeUnit.MILLISECONDS);
        }

        byte[] expectMessage() throws InterruptedException {
            var message = received.poll(EXPECT_MS, TimeUnit.MILLISECONDS);
            assertNotNull(message, "timed out waiting for the server to receive a message");
            return message;
        }

        @Override
        public void onOpen(@NotNull WebSocket webSocket, @NotNull Response response) {
            open.complete(webSocket);
        }

        @Override
        public void onMessage(@NotNull WebSocket webSocket, @NotNull ByteString bytes) {
            received.add(bytes.toByteArray());
            if (echo) {
                webSocket.send(bytes);
            }
        }

        @Override
        public void onClosing(@NotNull WebSocket webSocket, int code, @NotNull String reason) {
            closed.complete(code);
            // Complete the close handshake like a real CM; otherwise the server socket lingers
            webSocket.close(1000, null);
        }

        @Override
        public void onClosed(@NotNull WebSocket webSocket, int code, @NotNull String reason) {
            // The server started the close and the client answered it
            closed.complete(code);
        }
    }

    /**
     * Records what the connection reports, in order.
     */
    private static final class Events {

        final BlockingQueue<String> log = new LinkedBlockingQueue<>();

        final BlockingQueue<byte[]> messages = new LinkedBlockingQueue<>();

        final AtomicInteger disconnects = new AtomicInteger();

        // The endpoint as seen from inside the last Disconnected handler
        final AtomicReference<InetSocketAddress> endpointOnDisconnect = new AtomicReference<>();

        Events(Connection connection) {
            connection.getConnected().addEventHandler((sender, e) -> log.add("connected"));
            connection.getDisconnected().addEventHandler((sender, e) -> {
                disconnects.incrementAndGet();
                endpointOnDisconnect.set(connection.getCurrentEndPoint());
                log.add("disconnected:" + e.isUserInitiated());
            });
            connection.getNetMsgReceived().addEventHandler((sender, e) -> messages.add(e.getData()));
        }

        void expect(String event) throws InterruptedException {
            assertEquals(event, log.poll(EXPECT_MS, TimeUnit.MILLISECONDS));
        }

        byte[] expectMessage() throws InterruptedException {
            var message = messages.poll(EXPECT_MS, TimeUnit.MILLISECONDS);
            assertNotNull(message, "timed out waiting for a message");
            return message;
        }

        void expectQuiet() throws InterruptedException {
            assertNull(log.poll(QUIET_MS, TimeUnit.MILLISECONDS), "unexpected connection event");
            assertNull(messages.poll(), "unexpected message");
        }
    }

    // endregion
}
