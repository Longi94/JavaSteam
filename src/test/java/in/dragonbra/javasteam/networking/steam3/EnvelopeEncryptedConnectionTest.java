 package in.dragonbra.javasteam.networking.steam3;

import in.dragonbra.javasteam.TestBase;
import in.dragonbra.javasteam.base.Msg;
import in.dragonbra.javasteam.enums.EResult;
import in.dragonbra.javasteam.enums.EUniverse;
import in.dragonbra.javasteam.generated.MsgChannelEncryptRequest;
import in.dragonbra.javasteam.generated.MsgChannelEncryptResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Ported from SteamKit2 EnvelopeEncryptedConnectionFacts (SK PR 1658)
 */
public class EnvelopeEncryptedConnectionTest extends TestBase {

    private static final SecureRandom RANDOM = new SecureRandom();

    private static final class FakeConnection extends Connection {
        private static final InetSocketAddress FAKE_END_POINT = new InetSocketAddress(InetAddress.getLoopbackAddress(), 27015);

        final List<Boolean> disconnects = new ArrayList<>();

        @Override
        public void connect(@NotNull InetSocketAddress endPoint, int timeout) {
        }

        @Override
        public void disconnect(boolean userInitiated) {
            disconnects.add(userInitiated);
        }

        @Override
        public void send(byte @NotNull [] data) {
        }

        @Override
        public @NotNull InetAddress getLocalIP() {
            return InetAddress.getLoopbackAddress();
        }

        @Override
        public @Nullable InetSocketAddress getCurrentEndPoint() {
            return FAKE_END_POINT;
        }

        @Override
        public @Nullable ProtocolTypes getProtocolTypes() {
            return ProtocolTypes.TCP;
        }

        void raiseConnected() {
            onConnected();
        }

        void raiseDisconnected() {
            onDisconnected(false);
        }

        void raiseNetMsgReceived(byte[] data) {
            onNetMsgReceived(new NetMsgEventArgs(data, FAKE_END_POINT));
        }
    }

    private static byte[] randomBytes(int length) {
        var bytes = new byte[length];
        RANDOM.nextBytes(bytes);
        return bytes;
    }

    private static byte[] serializeEncryptRequest() throws IOException {
        var request = new Msg<MsgChannelEncryptRequest>(MsgChannelEncryptRequest.class);
        request.getBody().setUniverse(EUniverse.Public);
        request.getBody().setProtocolVersion(1);
        request.writeBytes(randomBytes(16));

        return request.serialize();
    }

    private static byte[] serializeEncryptResult() {
        var result = new Msg<MsgChannelEncryptResult>(MsgChannelEncryptResult.class);
        result.getBody().setResult(EResult.OK);

        return result.serialize();
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 15, 16, 20, 48})
    public void disconnectsWhenEncryptedPacketCannotBeDecrypted(int length) throws IOException {
        var inner = new FakeConnection();
        var connection = new EnvelopeEncryptedConnection(inner, EUniverse.Public, null);

        var connectedCount = new AtomicInteger();
        var receivedCount = new AtomicInteger();
        connection.getConnected().addEventHandler((sender, e) -> connectedCount.incrementAndGet());
        connection.getNetMsgReceived().addEventHandler((sender, e) -> receivedCount.incrementAndGet());

        inner.raiseConnected();
        inner.raiseNetMsgReceived(serializeEncryptRequest());
        inner.raiseNetMsgReceived(serializeEncryptResult());

        Assertions.assertEquals(1, connectedCount.get());
        Assertions.assertTrue(inner.disconnects.isEmpty());

        var garbage = randomBytes(length);
        Assertions.assertDoesNotThrow(() -> inner.raiseNetMsgReceived(garbage));

        Assertions.assertEquals(0, receivedCount.get());
        Assertions.assertEquals(List.of(false), inner.disconnects);
    }

    @Test
    public void ignoresPacketsAfterDisconnected() {
        var inner = new FakeConnection();
        new EnvelopeEncryptedConnection(inner, EUniverse.Public, null);

        inner.raiseConnected();
        inner.raiseDisconnected();

        var garbage = randomBytes(48);
        Assertions.assertDoesNotThrow(() -> inner.raiseNetMsgReceived(garbage));

        Assertions.assertTrue(inner.disconnects.isEmpty());
    }
}
