package in.dragonbra.javasteam.steam.webapi;

import in.dragonbra.javasteam.TestBase;
import in.dragonbra.javasteam.steam.steamclient.configuration.SteamConfiguration;
import in.dragonbra.javasteam.types.KeyValue;
import in.dragonbra.javasteam.util.Versions;
import mockwebserver3.MockResponse;
import mockwebserver3.MockWebServer;
import mockwebserver3.RecordedRequest;
import okhttp3.HttpUrl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author lngtr
 * @since 2018-04-09
 */
public class WebAPITest extends TestBase {

    private MockWebServer server;

    private HttpUrl baseUrl;

    private SteamConfiguration config;

    private CountDownLatch lock;

    @BeforeEach
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

        config = SteamConfiguration.create(b -> b.withWebAPIBaseAddress(baseUrl.toString()));
    }

    @AfterEach
    public void tearDown() throws IOException {
        server.shutdown();
    }

    @Test
    public void requestHeaders() throws InterruptedException, IOException {
        WebAPI api = config.getWebAPI("TestInterface");

        api.call("TestFunction");

        RecordedRequest request = server.takeRequest();

        assertEquals("JavaSteam-" + Versions.getVersion(), request.getHeaders().get("User-Agent"));
    }

    @Test
    public void steamConfigWebApiInterface() {
        SteamConfiguration config = SteamConfiguration.create(b ->
                b.withWebAPIBaseAddress("http://example.com/").withWebAPIKey("hello")
        );

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

        api.call("TestFunction", result -> {
            assertEquals("stringvalue", result.get("name").asString());
            assertEquals("stringvalue", result.get("name").getValue());
            lock.countDown();
        }, null);

        RecordedRequest request = server.takeRequest();
        assertEquals("/TestInterface/TestFunction/v1?format=vdf", request.getPath());
        assertEquals("GET", request.getMethod());

        //noinspection ResultOfMethodCallIgnored
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
        assertEquals("format=vdf", request.getBody().readString(StandardCharsets.UTF_8));
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
        assertEquals("key1=value1&key2=value2&format=vdf", request.getBody().readString(StandardCharsets.UTF_8));
    }

    @Test
    public void testNullMethod() {
        assertThrows(IllegalArgumentException.class, () -> {
            WebAPI api = config.getWebAPI("TestInterface");
            api.call(null, "TestFunction");
        });
    }

    @Test
    public void testNullFunction() {
        assertThrows(IllegalArgumentException.class, () -> {
            WebAPI api = config.getWebAPI("TestInterface");
            api.call("GET", (String) null);
        });
    }
}