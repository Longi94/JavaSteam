package `in`.dragonbra.javasteam.depotdownloader

import `in`.dragonbra.javasteam.util.Versions
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.engine.cio.endpoint
import io.ktor.client.plugins.UserAgent
import kotlinx.coroutines.isActive

/**
 * Singleton HTTP client for content downloader operations.
 * Provides a shared, configured Ktor HTTP client optimized for Steam CDN downloads.
 * The client is lazily initialized on first use and reused across all download operations.
 * Connection pooling and timeouts are configured based on the maximum concurrent downloads.
 *
 * @author Lossy
 * @since Oct 1, 2025
 */
object HttpClient {

    private var httpClient: HttpClient? = null

    fun getClient(maxConnections: Int = 8): HttpClient {
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

    fun close() {
        if (httpClient?.isActive == true) {
            httpClient?.close()
            httpClient = null
        }
    }
}
