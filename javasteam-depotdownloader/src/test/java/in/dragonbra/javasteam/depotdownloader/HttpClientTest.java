package in.dragonbra.javasteam.depotdownloader;

import mockwebserver3.MockResponse;
import mockwebserver3.MockWebServer;
import mockwebserver3.RecordedRequest;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class HttpClientTest {

    @Test
    public void appliesTheLimit() {
        // One number for both: all downloads may go to a single host, such as a LAN cache.
        var client = HttpClient.INSTANCE.create(new OkHttpClient(), 50);
        try {
            Assertions.assertEquals(50, client.dispatcher().getMaxRequests());
            Assertions.assertEquals(50, client.dispatcher().getMaxRequestsPerHost());
        } finally {
            HttpClient.INSTANCE.shutdown(client);
        }
    }

    @Test
    public void treatsANonPositiveLimitAsOne() {
        for (int maxDownloads : new int[]{0, -3}) {
            var client = HttpClient.INSTANCE.create(new OkHttpClient(), maxDownloads);
            try {
                Assertions.assertEquals(1, client.dispatcher().getMaxRequests());
                Assertions.assertEquals(1, client.dispatcher().getMaxRequestsPerHost());
            } finally {
                HttpClient.INSTANCE.shutdown(client);
            }
        }
    }

    @Test
    public void keepsTheSettingsOfTheBaseClientAndLeavesItAlone() {
        var base = new OkHttpClient.Builder()
                .readTimeout(42, TimeUnit.SECONDS)
                .addInterceptor(chain -> chain.proceed(chain.request()))
                .build();

        var client = HttpClient.INSTANCE.create(base, 16);
        HttpClient.INSTANCE.shutdown(client);

        Assertions.assertEquals(42_000, client.readTimeoutMillis());
        Assertions.assertEquals(1, client.interceptors().size());

        // Its own dispatcher and connection pool, so shutting it down does not touch the base client.
        Assertions.assertNotSame(base.dispatcher(), client.dispatcher());
        Assertions.assertNotSame(base.connectionPool(), client.connectionPool());
        Assertions.assertTrue(client.dispatcher().executorService().isShutdown());
        Assertions.assertFalse(base.dispatcher().executorService().isShutdown());
        Assertions.assertEquals(64, base.dispatcher().getMaxRequests());
        Assertions.assertEquals(5, base.dispatcher().getMaxRequestsPerHost());
    }

    @Test
    public void runsMoreThanFiveRequestsAgainstOneHost() throws Exception {
        // OkHttp's default is 5 per host, which would cap this at 5.
        Assertions.assertEquals(12, peakConcurrency(12, 24));
    }

    @Test
    public void aLowSettingLimitsTheRequests() throws Exception {
        Assertions.assertEquals(2, peakConcurrency(2, 24));
    }

    /**
     * Fires [requests] requests at one server and returns how many it had in flight at the same time at most.
     */
    private static int peakConcurrency(int maxDownloads, int requests) throws Exception {
        var inFlight = new AtomicInteger();
        var peak = new AtomicInteger();

        try (var server = new MockWebServer()) {
            server.setDispatcher(new mockwebserver3.Dispatcher() {
                @NotNull
                @Override
                public MockResponse dispatch(@NotNull RecordedRequest request) throws InterruptedException {
                    var now = inFlight.incrementAndGet();
                    peak.accumulateAndGet(now, Math::max);
                    Thread.sleep(150);
                    inFlight.decrementAndGet();
                    return new MockResponse.Builder().code(200).body("chunk").build();
                }
            });
            server.start();

            var client = HttpClient.INSTANCE.create(new OkHttpClient(), maxDownloads);
            try {
                var done = new CountDownLatch(requests);
                var failures = new AtomicInteger();

                for (int i = 0; i < requests; i++) {
                    var request = new Request.Builder().url(server.url("/depot/1/chunk/" + i)).build();
                    client.newCall(request).enqueue(new Callback() {
                        @Override
                        public void onFailure(@NotNull Call call, @NotNull IOException e) {
                            failures.incrementAndGet();
                            done.countDown();
                        }

                        @Override
                        public void onResponse(@NotNull Call call, @NotNull Response response) {
                            response.close();
                            done.countDown();
                        }
                    });
                }

                Assertions.assertTrue(done.await(30, TimeUnit.SECONDS), "requests did not finish");
                Assertions.assertEquals(0, failures.get());
            } finally {
                HttpClient.INSTANCE.shutdown(client);
            }
        }

        return peak.get();
    }
}
