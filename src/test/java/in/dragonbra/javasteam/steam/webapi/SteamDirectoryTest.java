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

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * @author lngtr
 * @since 2018-04-09
 */
public class SteamDirectoryTest extends TestBase {

    @Test
    public void load() {
        try (MockWebServer server = new MockWebServer()) {

            URL vdf = WebAPITest.class.getClassLoader().getResource("testresponses/GetCMListForConnect.vdf");

            if (vdf == null) {
                fail("finding 'testresponses/GetCMListForConnect.vdf' was null");
            }

            String resource = IOUtils.toString(vdf, StandardCharsets.UTF_8);

            MockResponse resp = new MockResponse().newBuilder().body(resource).build();
            server.enqueue(resp);

            server.start();

            final HttpUrl baseUrl = server.url("/");

            SteamConfiguration config = SteamConfiguration.create(b -> b.withWebAPIBaseAddress(baseUrl.toString()));

            List<ServerRecord> servers = SteamDirectory.load(config);

            assertEquals(80, servers.size());

            RecordedRequest request = server.takeRequest();
            assertEquals("/ISteamDirectory/GetCMListForConnect/v1?format=vdf&cellid=0", request.getPath());
            assertEquals("GET", request.getMethod());

            server.shutdown();
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }
}
