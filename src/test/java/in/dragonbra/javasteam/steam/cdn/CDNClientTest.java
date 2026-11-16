package in.dragonbra.javasteam.steam.cdn;

import in.dragonbra.javasteam.steam.steamclient.SteamClient;
import in.dragonbra.javasteam.steam.steamclient.configuration.SteamConfiguration;
import in.dragonbra.javasteam.types.ChunkData;
import in.dragonbra.javasteam.util.SteamKitWebRequestException;
import kotlin.coroutines.EmptyCoroutineContext;
import kotlinx.coroutines.BuildersKt;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.net.InetAddress;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;

/**
 * @author Lossy
 * @since 31/12/2024
 */
public class CDNClientTest {

    static class TeapotInterceptor implements Interceptor {
        @Override
        public @NotNull Response intercept(Chain chain) {
            return new Response.Builder()
                    .code(418)
                    .protocol(Protocol.HTTP_1_1)
                    .message("I'm a teapot")
                    .request(chain.request())
                    .body(ResponseBody.create(new byte[0], null))
                    .build();
        }
    }

    @Test
    public void throwsSteamKitWebExceptionOnUnsuccessfulWebResponseForManifest() {
        OkHttpClient httpClient = new OkHttpClient.Builder()
                .addInterceptor(new TeapotInterceptor())
                .build();

        var configuration = SteamConfiguration.create(x -> x.withHttpClient(httpClient));
        var steam = new SteamClient(configuration);
        try (var client = new Client(steam)) {
            var server = Server.fromHostAndPort("localhost", 80);

            // JVM will throw ExecutionException
            Exception exception = Assertions.assertThrows(ExecutionException.class, () ->
                    client.downloadManifestFuture(0, 0, 0, server).get()
            );

            // Get the actual instance to verify test.
            Assertions.assertInstanceOf(SteamKitWebRequestException.class, exception.getCause());
            Assertions.assertTrue(exception.getMessage().contains("418"));
        }
    }

    @Test
    public void throwsSteamKitWebExceptionOnUnsuccessfulWebResponseForChunk() {
        OkHttpClient httpClient = new OkHttpClient.Builder()
                .addInterceptor(new TeapotInterceptor())
                .build();

        var configuration = SteamConfiguration.create(x -> x.withHttpClient(httpClient));
        var steam = new SteamClient(configuration);
        try (var client = new Client(steam)) {
            var server = Server.fromHostAndPort("localhost", 80);
            var chunk = new ChunkData(new byte[]{(byte) 0xFF}, 0, 0L, 0, 0);

            // JVM will throw ExecutionException
            Exception exception = Assertions.assertThrows(ExecutionException.class, () ->
                    client.downloadDepotChunkFuture(0, chunk, server, new byte[0]).get()
            );

            // Get the actual instance to verify test.
            Assertions.assertInstanceOf(SteamKitWebRequestException.class, exception.getCause());
            Assertions.assertTrue(exception.getMessage().contains("418"));
        }
    }

    @Test
    public void closingOneClientLeavesTheSharedHttpClientUsable() {
        OkHttpClient httpClient = new OkHttpClient.Builder()
                .addInterceptor(new TeapotInterceptor())
                .build();

        var configuration = SteamConfiguration.create(x -> x.withHttpClient(httpClient));
        var steam = new SteamClient(configuration);
        var server = Server.fromHostAndPort("localhost", 80);

        // The HTTP client belongs to the configuration, closing a CDN client must not shut it down.
        new Client(steam).close();

        Assertions.assertFalse(httpClient.dispatcher().executorService().isShutdown());

        try (var client = new Client(steam)) {
            Exception exception = Assertions.assertThrows(ExecutionException.class, () ->
                    client.downloadManifestFuture(0, 0, 0, server).get()
            );

            // The request still reaches the server (the interceptor), it is not rejected by a dead executor.
            Assertions.assertInstanceOf(SteamKitWebRequestException.class, exception.getCause());
            Assertions.assertTrue(exception.getMessage().contains("418"));
        }
    }

    @Test
    public void throwsWhenNoChunkIDIsSet() {
        OkHttpClient httpClient = new OkHttpClient.Builder()
                .addInterceptor(new TeapotInterceptor())
                .build();

        var configuration = SteamConfiguration.create(x -> x.withHttpClient(httpClient));
        var steam = new SteamClient(configuration);
        try (var client = new Client(steam)) {
            var server = Server.fromHostAndPort("localhost", 80);
            var chunk = new ChunkData();

            // JVM will throw ExecutionException
            Exception exception = Assertions.assertThrows(ExecutionException.class, () ->
                    client.downloadDepotChunkFuture(0, chunk, server, new byte[0]).get()
            );

            // Get the actual instance to verify test.
            Assertions.assertInstanceOf(IllegalArgumentException.class, exception.getCause());
            // JVM doesn't have a paramName for exception checking, so we'll check if the message contains something about the chunk.
            Assertions.assertTrue(exception.getMessage().toLowerCase().contains("chunk"));
        }
    }

    @Test
    public void throwsWhenDestinationBufferSmaller() {
        OkHttpClient httpClient = new OkHttpClient.Builder()
                .addInterceptor(new TeapotInterceptor())
                .build();

        var configuration = SteamConfiguration.create(x -> x.withHttpClient(httpClient));
        var steam = new SteamClient(configuration);
        try (var client = new Client(steam)) {
            var server = Server.fromHostAndPort("localhost", 80);
            var chunk = new ChunkData(new byte[]{(byte) 0xFF}, 0, 0, 32, 64);

            // JVM will throw ExecutionException
            Exception exception = Assertions.assertThrows(ExecutionException.class, () ->
                    client.downloadDepotChunkFuture(0, chunk, server, new byte[4]).get()
            );

            // Get the actual instance to verify test.
            Assertions.assertInstanceOf(IllegalArgumentException.class, exception.getCause());
            // JVM doesn't have a paramName for exception checking, so we'll check if the message contains something about the destination.
            Assertions.assertTrue(exception.getMessage().toLowerCase().contains("destination"));
        }
    }

    @Test
    public void throwsWhenDestinationBufferSmallerWithDepotKey() {
        OkHttpClient httpClient = new OkHttpClient.Builder()
                .addInterceptor(new TeapotInterceptor())
                .build();

        var configuration = SteamConfiguration.create(x -> x.withHttpClient(httpClient));
        var steam = new SteamClient(configuration);
        try (var client = new Client(steam)) {
            var server = Server.fromHostAndPort("localhost", 80);
            var chunk = new ChunkData(new byte[]{(byte) 0xFF}, 0, 0, 32, 64);

            // JVM will throw ExecutionException
            Exception exception = Assertions.assertThrows(ExecutionException.class, () ->
                    client.downloadDepotChunkFuture(0, chunk, server, new byte[4], new byte[0]).get()
            );

            // Get the actual instance to verify test.
            Assertions.assertInstanceOf(IllegalArgumentException.class, exception.getCause());
            // JVM doesn't have a paramName for exception checking, so we'll check if the message contains something about the destination.
            Assertions.assertTrue(exception.getMessage().toLowerCase().contains("destination"));
        }
    }

    /** Answers every request with the given bytes as the body. */
    static class BodyInterceptor implements Interceptor {
        private final byte[] body;

        BodyInterceptor(byte[] body) {
            this.body = body;
        }

        @Override
        public @NotNull Response intercept(Chain chain) {
            return new Response.Builder()
                    .code(200)
                    .protocol(Protocol.HTTP_1_1)
                    .message("OK")
                    .request(chain.request())
                    .body(ResponseBody.create(body, null))
                    .build();
        }
    }

    /** Calls the suspending downloadDepotChunk and waits for it. */
    private static int downloadChunk(Client client, ChunkData chunk, byte[] destination) throws InterruptedException {
        var server = Server.fromHostAndPort("localhost", 80);
        return BuildersKt.runBlocking(
                EmptyCoroutineContext.INSTANCE,
                (scope, continuation) -> client.downloadDepotChunk(0, chunk, server, destination, null, null, null, continuation)
        );
    }

    private static byte[] sequence(int length) {
        var bytes = new byte[length];
        for (int i = 0; i < length; i++) {
            bytes[i] = (byte) (i + 1);
        }
        return bytes;
    }

    @Test
    public void downloadsChunkIntoADestinationLongerThanTheChunk() throws InterruptedException {
        var body = sequence(48);

        OkHttpClient httpClient = new OkHttpClient.Builder()
                .addInterceptor(new BodyInterceptor(body))
                .build();

        var configuration = SteamConfiguration.create(x -> x.withHttpClient(httpClient));
        var steam = new SteamClient(configuration);
        try (var client = new Client(steam)) {
            var chunk = new ChunkData(new byte[]{(byte) 0xFF}, 0, 0L, body.length, 64);

            // What a pooled buffer looks like: longer than the chunk, with old content in it.
            var destination = new byte[100];
            Arrays.fill(destination, (byte) 0x7F);

            var written = downloadChunk(client, chunk, destination);

            Assertions.assertEquals(body.length, written);
            Assertions.assertArrayEquals(body, Arrays.copyOf(destination, written));

            // Nothing is written past the chunk.
            for (int i = written; i < destination.length; i++) {
                Assertions.assertEquals((byte) 0x7F, destination[i]);
            }
        }
    }

    @Test
    public void downloadsChunkIntoADestinationOfExactlyTheChunkLength() throws InterruptedException {
        var body = sequence(48);

        OkHttpClient httpClient = new OkHttpClient.Builder()
                .addInterceptor(new BodyInterceptor(body))
                .build();

        var configuration = SteamConfiguration.create(x -> x.withHttpClient(httpClient));
        var steam = new SteamClient(configuration);
        try (var client = new Client(steam)) {
            var chunk = new ChunkData(new byte[]{(byte) 0xFF}, 0, 0L, body.length, 64);
            var destination = new byte[body.length];

            Assertions.assertEquals(body.length, downloadChunk(client, chunk, destination));
            Assertions.assertArrayEquals(body, destination);
        }
    }

    @Test
    public void rejectsAChunkBodyOfTheWrongLength() {
        OkHttpClient httpClient = new OkHttpClient.Builder()
                .addInterceptor(new BodyInterceptor(sequence(40)))
                .build();

        var configuration = SteamConfiguration.create(x -> x.withHttpClient(httpClient));
        var steam = new SteamClient(configuration);
        try (var client = new Client(steam)) {
            // The chunk says 48 bytes, the server sends 40. The longer destination must not hide that.
            var chunk = new ChunkData(new byte[]{(byte) 0xFF}, 0, 0L, 48, 64);

            Assertions.assertThrows(SteamKitWebRequestException.class, () -> downloadChunk(client, chunk, new byte[100]));
        }
    }

    @Test
    public void rejectsADestinationShorterThanTheChunk() {
        OkHttpClient httpClient = new OkHttpClient.Builder()
                .addInterceptor(new BodyInterceptor(sequence(48)))
                .build();

        var configuration = SteamConfiguration.create(x -> x.withHttpClient(httpClient));
        var steam = new SteamClient(configuration);
        try (var client = new Client(steam)) {
            var chunk = new ChunkData(new byte[]{(byte) 0xFF}, 0, 0L, 48, 64);

            Assertions.assertThrows(IllegalArgumentException.class, () -> downloadChunk(client, chunk, new byte[47]));
        }
    }

    @ParameterizedTest
    @CsvSource({
            "10.0.0.1, true",      // Private IPv4 (10.0.0.0/8)
            "172.16.0.1, true",    // Private IPv4 (172.16.0.0/12)
            "192.168.0.1, true",   // Private IPv4 (192.168.0.0/16)
            "8.8.8.8, false",      // Public IPv4
            "127.0.0.1, true"      // Loopback IPv4
    })
    public void testPrivateIPv4Addresses(String ipAddress, boolean expected) throws Exception {
        InetAddress address = InetAddress.getByName(ipAddress);
        boolean result = ClientLancache.isPrivateAddress(address);
        Assertions.assertEquals(expected, result);
    }

    @ParameterizedTest
    @CsvSource({
            "fc00::1, true",       // Private IPv6 (Unique Local Address)
            "fe80::1, true",       // Link-local IPv6
            "2001:db8::1, false",  // Public IPv6
            "::1, true"           // Loopback IPv6
    })
    public void testPrivateIPv6Addresses(String ipAddress, boolean expected) throws Exception {
        InetAddress address = InetAddress.getByName(ipAddress);
        boolean result = ClientLancache.isPrivateAddress(address);
        Assertions.assertEquals(expected, result);
    }

    @ParameterizedTest
    @CsvSource({
            "token=abc,          token,  ",          // normal, no change
            "?token=abc,         token,  ?token",    // leading ? stripped
            "??token=abc,        token,  ??token",   // multiple leading ? stripped
            "?token=abc&foo=bar, token,  ?token",    // leading ? with multiple params
    })
    public void buildCommandStripsLeadingQuestionMarkFromQueryString(String query, String expectedKey, String unexpectedKey) {
        var server = Server.fromHostAndPort("localhost", 80);
        HttpUrl url = Client.buildCommand(server, "depot/731/manifest", query, null);

        Assertions.assertNotNull(url.queryParameter(expectedKey));
        if (unexpectedKey != null) {
            Assertions.assertNull(url.queryParameter(unexpectedKey));
        }
    }
}
