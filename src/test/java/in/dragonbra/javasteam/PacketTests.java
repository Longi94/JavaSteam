package in.dragonbra.javasteam;

import in.dragonbra.javasteam.base.PacketClientMsgProtobuf;
import in.dragonbra.javasteam.enums.EMsg;
import in.dragonbra.javasteam.steam.CMClient;
import in.dragonbra.javasteam.steam.handlers.steamapps.callback.PICSProductInfoCallback;
import in.dragonbra.javasteam.steam.steamclient.SteamClient;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * From SK PR #1403
 * > Packets were captured with an anonymous login. This only tests that the expected callback was posted to the client.
 * > This could allow us to quickly increase test coverage of all the handlers.
 * > We can add more intricate tests later for specific callbacks.
 *
 * @author Lossy
 * @since 31/12/2024
 */
public class PacketTests {

    private static final class TestPacket {
        private final EMsg eMsg;
        private final byte[] data;

        public TestPacket(EMsg eMsg, byte[] data) {
            this.eMsg = eMsg;
            this.data = data;
        }

        public EMsg getEMsg() {
            return eMsg;
        }

        public byte[] getData() {
            return data;
        }
    }

    @SuppressWarnings({"EnumSwitchStatementWhichMissesCases", "SwitchStatementWithTooFewBranches"})
    private static Class<?> getCallback(EMsg msg) {
        switch (msg) {
            case ClientPICSProductInfoResponse:
                return PICSProductInfoCallback.class;
            default:
                return null;
        }
    }

    @SuppressWarnings("DataFlowIssue")
    @Test
    public void postsExpectedCallbacks() {
        var steamClient = new SteamClient();

        getPackets("in").forEach(packet -> {
            var expectedCallback = getCallback(packet.getEMsg());
            Assertions.assertNotNull(expectedCallback);

            var packetMsg = CMClient.getPacketMsg(packet.getData());
            Assertions.assertNotNull(packetMsg);

            Assertions.assertInstanceOf(PacketClientMsgProtobuf.class, packetMsg);

            Assertions.assertNull(steamClient.getCallback());
            steamClient.receiveTestPacketMsg(packetMsg);

            var callback = steamClient.getCallback();
            Assertions.assertNotNull(callback);
            Assertions.assertEquals(expectedCallback, callback.getClass());
        });
    }

    @SuppressWarnings("SameParameterValue")
    private Stream<TestPacket> getPackets(String direction) {
        var resourceUrl = getClass().getClassLoader().getResource("packets");
        if (resourceUrl == null) {
            Assertions.fail("Cannot find packets directory in resources");
        }

        try {
            var folder = Paths.get(resourceUrl.toURI());
            try (var pathStream = Files.list(folder)) {
                return pathStream
                        .filter(path -> path.toString().endsWith(".bin"))
                        .map(path -> getPacket(path, direction))
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList())
                        .stream();
            }
        } catch (IOException | URISyntaxException e) {
            Assertions.fail(e);
        }

        Assertions.fail("No packets found");
        return null;
    }

    private TestPacket getPacket(Path filepath, String direction) {
        try {
            var filename = filepath.getFileName().toString();
            var parts = filename.substring(0, filename.lastIndexOf('.')).split("_");

            Assertions.assertTrue(parts.length > 3, "Invalid filename format");

            if (!parts[1].equals(direction)) {
                return null;
            }

            var emsg = EMsg.from(Integer.parseUnsignedInt(parts[2]));
            var data = Files.readAllBytes(filepath);

            return new TestPacket(emsg, data);
        } catch (IOException e) {
            Assertions.fail("Error reading packet file", e);
        }

        Assertions.fail("Invalid packet file");
        return null;
    }
}
