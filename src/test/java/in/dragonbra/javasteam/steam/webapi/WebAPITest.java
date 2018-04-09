package in.dragonbra.javasteam.steam.webapi;

import in.dragonbra.javasteam.steam.steamclient.configuration.ISteamConfigurationBuilder;
import in.dragonbra.javasteam.steam.steamclient.configuration.SteamConfiguration;
import in.dragonbra.javasteam.types.KeyValue;
import in.dragonbra.javasteam.util.compat.Consumer;
import okhttp3.HttpUrl;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;

/**
 * @author lngtr
 * @since 2018-04-09
 */
public class WebAPITest {

    private MockWebServer server;

    private HttpUrl baseUrl;

    private SteamConfiguration config;

    private CountDownLatch lock;

    @Before
    public void setUp() throws IOException {
        lock = new CountDownLatch(1);
        server = new MockWebServer();

        server.enqueue(new MockResponse().setBody("" +
                "\"root\"" +
                "{" +
                "    \"name\" \"stringvalue\"" +
                "}"));

        server.start();

        baseUrl = server.url("/");

        config = SteamConfiguration.create(new Consumer<ISteamConfigurationBuilder>() {
            @Override
            public void accept(ISteamConfigurationBuilder b) {
                b.withWebAPIBaseAddress(baseUrl.toString());
            }
        });
    }

    @After
    public void tearDown() throws IOException {
        server.shutdown();
    }

    @Test
    public void steamConfigWebApiInterface() {
        SteamConfiguration config = SteamConfiguration.create(new Consumer<ISteamConfigurationBuilder>() {
            @Override
            public void accept(ISteamConfigurationBuilder b) {
                b.withWebAPIBaseAddress("http://example.com/")
                        .withWebAPIKey("hello");
            }
        });

        WebAPI api = config.getWebAPI("TestInterface");

        assertEquals(api.getInterface(), "TestInterface");
        assertEquals(api.getWebAPIKey(), "hello");
        assertEquals(api.getBaseAddress().toString(), "http://example.com/");
    }

    @Test
    public void testSyncCall() throws IOException, InterruptedException {
        WebAPI api = config.getWebAPI("TestInterface");

        KeyValue result = api.call("TestFunction");

        assertEquals("stringvalue", result.get("name").asString());
        assertEquals("stringvalue", result.get("name").getValue());

        RecordedRequest request = server.takeRequest();
        assertEquals("/TestInterface/TestFunction/v1?format=vdf", request.getPath());
        assertEquals("GET", request.getMethod());
    }

    @Test
    public void testAsyncCall() throws IOException, InterruptedException {
        WebAPI api = config.getWebAPI("TestInterface");

        api.call("TestFunction", new Consumer<KeyValue>() {
            @Override
            public void accept(KeyValue result) {
                assertEquals("stringvalue", result.get("name").asString());
                assertEquals("stringvalue", result.get("name").getValue());
                lock.countDown();
            }
        });

        RecordedRequest request = server.takeRequest();
        assertEquals("/TestInterface/TestFunction/v1?format=vdf", request.getPath());
        assertEquals("GET", request.getMethod());

        lock.await(2000, TimeUnit.MILLISECONDS);
    }

    @Test
    public void testPostCall() throws IOException, InterruptedException {
        WebAPI api = config.getWebAPI("TestInterface");

        KeyValue result = api.call("POST", "TestFunction");

        assertEquals("stringvalue", result.get("name").asString());
        assertEquals("stringvalue", result.get("name").getValue());

        RecordedRequest request = server.takeRequest();
        assertEquals("/TestInterface/TestFunction/v1", request.getPath());
        assertEquals("POST", request.getMethod());
        assertEquals("format=vdf", request.getBody().readString(Charset.forName("UTF-8")));
    }

    @Test
    public void testVersionCall() throws IOException, InterruptedException {
        WebAPI api = config.getWebAPI("TestInterface");

        KeyValue result = api.call("TestFunction", 69);

        assertEquals("stringvalue", result.get("name").asString());
        assertEquals("stringvalue", result.get("name").getValue());

        RecordedRequest request = server.takeRequest();
        assertEquals("/TestInterface/TestFunction/v69?format=vdf", request.getPath());
        assertEquals("GET", request.getMethod());
    }

    @Test
    public void testParametersCall() throws IOException, InterruptedException {
        WebAPI api = config.getWebAPI("TestInterface");

        Map<String, String> params = new HashMap<>();
        params.put("key1", "value1");
        params.put("key2", "value2");

        KeyValue result = api.call("TestFunction", params);

        assertEquals("stringvalue", result.get("name").asString());
        assertEquals("stringvalue", result.get("name").getValue());

        RecordedRequest request = server.takeRequest();
        assertEquals("/TestInterface/TestFunction/v1?key1=value1&key2=value2&format=vdf", request.getPath());
        assertEquals("GET", request.getMethod());
    }

    @Test
    public void testParametersPostCall() throws IOException, InterruptedException {
        WebAPI api = config.getWebAPI("TestInterface");

        Map<String, String> params = new HashMap<>();
        params.put("key1", "value1");
        params.put("key2", "value2");

        KeyValue result = api.call("POST", "TestFunction", params);

        assertEquals("stringvalue", result.get("name").asString());
        assertEquals("stringvalue", result.get("name").getValue());

        RecordedRequest request = server.takeRequest();
        assertEquals("/TestInterface/TestFunction/v1", request.getPath());
        assertEquals("POST", request.getMethod());
        assertEquals("key1=value1&key2=value2&format=vdf", request.getBody().readString(Charset.forName("UTF-8")));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullMethod() throws IOException {
        WebAPI api = config.getWebAPI("TestInterface");
        api.call(null, "TestFunction");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullFunction() throws IOException {
        WebAPI api = config.getWebAPI("TestInterface");
        api.call("GET", (String) null);
    }
}