package `in`.dragonbra.javasteam.depotdownloader

import `in`.dragonbra.javasteam.util.Versions
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.engine.cio.endpoint
import io.ktor.client.plugins.UserAgent
import kotlinx.coroutines.isActive
import java.io.Closeable

/**
 * HTTP client wrapper for content downloader operations.
 *
 * Provides a configured Ktor HTTP client optimized for Steam CDN downloads.
 * Each instance maintains its own connection pool based on the specified
 * maximum concurrent connections.
 *
 * @param maxConnections Maximum number of concurrent connections
 *
 * @author Lossy
 * @since Oct 1, 2025
 */
class HttpClient(
    private val maxConnections: Int,
) : Closeable {

    private var httpClient: HttpClient? = null

    /**
     * Returns the HTTP client instance, creating it lazily on first access.
     *
     * The client is configured with:
     * - Custom User-Agent identifying JavaSteam DepotDownloader
     * - Connection pooling based on [maxConnections]
     * - 5 second keep-alive and connect timeout
     * - 30 second request timeout
     */
    fun getClient(): HttpClient {
        if (httpClient?.isActive != true) {
            httpClient = HttpClient(CIO) {
                install(UserAgent) {
                    agent = "JavaSteam-DepotDownloader/${Versions.getVersion()}"
                }
                engine {
                    maxConnectionsCount = maxConnections
                    endpoint {
                        maxConnectionsPerRoute = (maxConnections / 2).coerceAtLeast(1)
                        pipelineMaxSize = maxConnections * 2
                        keepAliveTime = 5000
                        connectTimeout = 5000
                        requestTimeout = 30000
                    }
                }
            }
        }

        return httpClient!!
    }

    override fun close() {
        if (httpClient?.isActive == true) {
            httpClient?.close()
            httpClient = null
        }
    }
}
