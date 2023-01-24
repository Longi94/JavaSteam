package in.dragonbra.javasteam.steam.webapi;

import in.dragonbra.javasteam.TestBase;
import in.dragonbra.javasteam.steam.discovery.ServerRecord;
import in.dragonbra.javasteam.steam.steamclient.configuration.SteamConfiguration;
import mockwebserver3.MockResponse;
import mockwebserver3.MockWebServer;
import mockwebserver3.RecordedRequest;
import okhttp3.HttpUrl;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author lngtr
 * @since 2018-04-09
 */
public class SteamDirectoryTest extends TestBase {

    @Test
    public void load() throws IOException, InterruptedException {
        MockWebServer server = new MockWebServer();

        String resource = IOUtils.toString(
                WebAPITest.class.getClassLoader().getResource("testresponses/GetCMList.vdf"),
                StandardCharsets.UTF_8
        );
        server.enqueue(new MockResponse().setBody(resource));

        server.start();

        final HttpUrl baseUrl = server.url("/");

        SteamConfiguration config = SteamConfiguration.create(b -> b.withWebAPIBaseAddress(baseUrl.toString()));

        List<ServerRecord> servers = SteamDirectory.load(config);

        assertEquals(200, servers.size());

        RecordedRequest request = server.takeRequest();
        assertEquals("/ISteamDirectory/GetCMList/v1?format=vdf&cellid=0", request.getPath());
        assertEquals("GET", request.getMethod());

        server.shutdown();
    }
}