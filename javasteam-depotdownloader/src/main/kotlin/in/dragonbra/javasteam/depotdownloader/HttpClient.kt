package `in`.dragonbra.javasteam.depotdownloader

import `in`.dragonbra.javasteam.util.Versions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.ConnectionPool
import okhttp3.Dispatcher
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.coroutines.executeAsync
import okio.FileSystem
import okio.Path
import okio.buffer
import java.util.concurrent.SynchronousQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

/**
 * The HTTP side of a [DepotDownloader]: builds the client it downloads with, and downloads plain web files.
 *
 * OkHttp runs at most 5 requests per host and 64 in total on a client, whatever the caller asks
 * for, and keeps 5 idle connections. The downloader therefore derives its own client from the one
 * in the SteamClient configuration: same timeouts, interceptors and proxy, but with request limits
 * and a connection pool sized from the downloader's settings. The original client is not changed.
 */
internal object HttpClient {

    /**
     * @param base The client to derive from.
     * @param maxDownloads Concurrent requests, in total and to a single host.
     */
    fun create(base: OkHttpClient, maxDownloads: Int): OkHttpClient {
        val total = maxDownloads.coerceAtLeast(1)

        // Same executor OkHttp creates by default, but with daemon threads, so a downloader that
        // was not closed does not keep the JVM alive for the 60 second keep-alive.
        val executor = ThreadPoolExecutor(0, Int.MAX_VALUE, 60, TimeUnit.SECONDS, SynchronousQueue()) { runnable ->
            Thread(runnable, "JavaSteam DepotDownloader HTTP").apply { isDaemon = true }
        }

        val dispatcher = Dispatcher(executor).apply {
            maxRequests = total
            maxRequestsPerHost = total
        }

        // Keep one connection per concurrent download alive between chunks.
        val pool = ConnectionPool(total, 5, TimeUnit.MINUTES)

        return base.newBuilder()
            .dispatcher(dispatcher)
            .connectionPool(pool)
            .build()
    }

    /**
     * Releases a client made by [create]. Never call this with the client it was derived from.
     */
    fun shutdown(client: OkHttpClient) {
        client.dispatcher.executorService.shutdown()
        client.connectionPool.evictAll()
    }

    /**
     * Downloads a plain web file, which is how some workshop and UGC items are served instead of
     * depot chunks, to [destination], replacing the file if it exists.
     *
     * @param httpClient The client to use. Its timeouts apply.
     * @param filesystem The file system [destination] is on.
     * @return The number of bytes written.
     * @throws DepotDownloaderException The server did not answer with a success status. Nothing is written.
     * @throws java.io.IOException A network or file error occurred.
     */
    suspend fun downloadFile(
        httpClient: OkHttpClient,
        url: String,
        destination: Path,
        filesystem: FileSystem = FileSystem.SYSTEM,
    ): Long {
        val request = Request.Builder()
            .url(url)
            .header("User-Agent", "JavaSteam-DepotDownloader/${Versions.getVersion()}")
            .build()

        return httpClient.newCall(request).executeAsync().use { response ->
            // Otherwise the body of an error page would be saved as the file.
            if (!response.isSuccessful) {
                throw DepotDownloaderException("Failed to download $url: HTTP ${response.code}")
            }

            withContext(Dispatchers.IO) {
                filesystem.sink(destination).buffer().use { sink ->
                    sink.writeAll(response.body.source())
                }
            }
        }
    }
}
