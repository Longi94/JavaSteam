package in.dragonbra.javasteam.steam.webapi;

import in.dragonbra.javasteam.steam.discovery.ServerRecord;
import in.dragonbra.javasteam.steam.steamclient.configuration.ISteamConfigurationBuilder;
import in.dragonbra.javasteam.steam.steamclient.configuration.SteamConfiguration;
import in.dragonbra.javasteam.util.compat.Consumer;
import okhttp3.HttpUrl;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * @author lngtr
 * @since 2018-04-09
 */
public class SteamDirectoryTest {

    @Test
    public void load() throws IOException, InterruptedException {
        MockWebServer server = new MockWebServer();

        server.enqueue(new MockResponse().setBody(IOUtils.toString(
                WebAPITest.class.getClassLoader().getResource("testresponses/GetCMList.vdf"), "UTF-8")));

        server.start();

        final HttpUrl baseUrl = server.url("/");

        SteamConfiguration config = SteamConfiguration.create(new Consumer<ISteamConfigurationBuilder>() {
            @Override
            public void accept(ISteamConfigurationBuilder b) {
                b.withWebAPIBaseAddress(baseUrl.toString());
            }
        });

        List<ServerRecord> servers = SteamDirectory.load(config);

        assertEquals(200, servers.size());

        RecordedRequest request = server.takeRequest();
        assertEquals("/ISteamDirectory/GetCMList/v1?format=vdf&cellid=0", request.getPath());
        assertEquals("GET", request.getMethod());

        server.shutdown();
    }
}