package in.dragonbra.javasteam.steam.discovery;

import in.dragonbra.javasteam.TestBase;
import in.dragonbra.javasteam.networking.steam3.ProtocolTypes;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class FileServerListProviderTest extends TestBase {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void testSaveAndRead() throws IOException {
        FileServerListProvider provider = new FileServerListProvider(folder.newFile());

        List<ServerRecord> serverRecords = new ArrayList<>();

        serverRecords.add(ServerRecord.createServer("162.254.197.42", 27017, ProtocolTypes.TCP));
        serverRecords.add(ServerRecord.createServer("162.254.197.42", 27018, ProtocolTypes.TCP));
        serverRecords.add(ServerRecord.createServer("162.254.197.42", 27017, ProtocolTypes.UDP));
        serverRecords.add(ServerRecord.createServer("CM02-FRA.cm.steampowered.com", 27017, ProtocolTypes.WEB_SOCKET));

        provider.updateServerList(serverRecords);

        List<ServerRecord> loadedList = provider.fetchServerList();

        assertThat(loadedList, is(serverRecords));
    }

    @Test
    public void testMissingFile() throws IOException {
        FileServerListProvider provider = new FileServerListProvider(folder.newFile());

        List<ServerRecord> serverRecords = provider.fetchServerList();

        assertTrue(serverRecords.isEmpty());
    }
}