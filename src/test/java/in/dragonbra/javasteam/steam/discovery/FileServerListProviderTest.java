package in.dragonbra.javasteam.steam.discovery;

import in.dragonbra.javasteam.TestBase;
import in.dragonbra.javasteam.networking.steam3.ProtocolTypes;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.EnumSet;
import java.util.List;

public class FileServerListProviderTest extends TestBase {

    @TempDir
    Path tempDir;

    @Test
    public void testInitializationWithPath() {
        var path = Path.of("servertest.bin");
        var provider = new FileServerListProvider(path);

        Assertions.assertEquals("servertest.bin", provider.getFile().getFileName().toString());
        Assertions.assertNotNull(provider);
    }

    @Test
    public void testInitializationWithFile() {
        var file = new File("servertest.bin");
        var provider = new FileServerListProvider(file);

        Assertions.assertEquals("servertest.bin", provider.getFile().getFileName().toString());
        Assertions.assertNotNull(provider);
    }

    @Test
    public void testInitializationWithString() {
        var fileName = "servertest.bin";
        var provider = new FileServerListProvider(fileName);

        Assertions.assertEquals("servertest.bin", provider.getFile().getFileName().toString());
        Assertions.assertNotNull(provider);
    }

    @Test
    public void testFetchServerListWithNoFile() {
        var file = tempDir.resolve("servertest.bin");
        var provider = new FileServerListProvider(file);

        var servers = provider.fetchServerList();

        Assertions.assertTrue(servers.isEmpty());
        Assertions.assertFalse(file.toFile().exists());
    }

    @Test
    public void testUpdateServerList() {
        var file = new File("servertest.bin");
        var provider = new FileServerListProvider(file);

        var serverRecords = List.of(
                ServerRecord.createServer("127.0.0.1", 8080, ProtocolTypes.TCP),
                ServerRecord.createServer("192.168.0.1", 8081, ProtocolTypes.UDP),
                ServerRecord.createServer("0.0.0.0", 8081, ProtocolTypes.WEB_SOCKET)
        );

        provider.updateServerList(serverRecords);

        var servers = provider.fetchServerList();

        Assertions.assertEquals(3, servers.size());
        Assertions.assertTrue(file.exists());
    }

    @Test
    public void testReadsUpdatedServerList() throws IOException {
        var fileStorageProvider = new FileServerListProvider("servertest.bin");
        fileStorageProvider.fetchServerList();

        fileStorageProvider.updateServerList(List.of(
                ServerRecord.createSocketServer(new InetSocketAddress(InetAddress.getByName("0.0.0.0"), 1234)),
                ServerRecord.createSocketServer(new InetSocketAddress(InetAddress.getLoopbackAddress(), 4321))
        ));

        var servers = fileStorageProvider.fetchServerList();
        Assertions.assertEquals(2, servers.size());

        ServerRecord firstServer = servers.get(0);
        Assertions.assertEquals("0.0.0.0", firstServer.getHost());
        Assertions.assertEquals(1234, firstServer.getPort());
        Assertions.assertEquals(EnumSet.of(ProtocolTypes.TCP, ProtocolTypes.UDP), firstServer.getProtocolTypes());

        var file = Path.of("servertest.bin");
        Files.deleteIfExists(file);
        Assertions.assertFalse(file.toFile().exists());
    }

    @Test
    public void testLastServerListRefresh() throws IOException {
        var tempFile = tempDir.resolve("servertest.bin");

        FileServerListProvider provider = new FileServerListProvider(tempFile);

        provider.updateServerList(List.of());

        var now = Instant.now().truncatedTo(ChronoUnit.SECONDS);
        var lastRefresh = provider.getLastServerListRefresh().truncatedTo(ChronoUnit.SECONDS);
        Assertions.assertEquals(now, lastRefresh);

        var file = Path.of("servertest.bin");
        Files.deleteIfExists(file);
        Assertions.assertFalse(file.toFile().exists());
    }
}
