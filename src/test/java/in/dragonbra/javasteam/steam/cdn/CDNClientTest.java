package in.dragonbra.javasteam.steam.cdn;

import in.dragonbra.javasteam.steam.steamclient.SteamClient;
import in.dragonbra.javasteam.steam.steamclient.configuration.SteamConfiguration;
import in.dragonbra.javasteam.types.ChunkData;
import in.dragonbra.javasteam.util.SteamKitWebRequestException;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.net.InetAddress;
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
}
