package in.dragonbra.javasteam.steam.discovery;

import in.dragonbra.javasteam.TestBase;
import in.dragonbra.javasteam.networking.steam3.ProtocolTypes;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


public class FileServerListProviderTest extends TestBase {

    @TempDir
    Path folder;

    @Test
    public void testSaveAndRead() throws IOException {
        final Path tempFile = Files.createFile(folder.resolve("FileServerListProvider.txt"));

        FileServerListProvider provider = new FileServerListProvider(tempFile.toFile());

        List<ServerRecord> serverRecords = new ArrayList<>();

        serverRecords.add(ServerRecord.createServer("162.254.197.42", 27017, ProtocolTypes.TCP));
        serverRecords.add(ServerRecord.createServer("162.254.197.42", 27018, ProtocolTypes.TCP));
        serverRecords.add(ServerRecord.createServer("162.254.197.42", 27017, ProtocolTypes.UDP));
        serverRecords.add(ServerRecord.createServer("CM02-FRA.cm.steampowered.com", 27017, ProtocolTypes.WEB_SOCKET));

        provider.updateServerList(serverRecords);

        List<ServerRecord> loadedList = provider.fetchServerList();

        assertEquals(loadedList, serverRecords);
    }

    @Test
    public void testMissingFile() throws IOException {
        final Path tempFile = Files.createFile(folder.resolve("FileServerListProvider.txt"));

        FileServerListProvider provider = new FileServerListProvider(tempFile.toFile());

        List<ServerRecord> serverRecords = provider.fetchServerList();

        assertTrue(serverRecords.isEmpty());
    }
}