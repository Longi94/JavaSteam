package `in`.dragonbra.javasteam.steam.cdn

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.Request
import java.net.Inet4Address
import java.net.Inet6Address
import java.net.InetAddress

/**
 * @author Lossy
 * @since 31/12/2024
 */
object ClientLancache {
    /**
     * When set to true, will attempt to download from a Lancache instance on the LAN
     * rather than going out to Steam's CDNs.
     */
    var useLanCacheServer: Boolean = false
        private set

    private const val TRIGGER_DOMAIN = "lancache.steamcontent.com"

    /**
     * Attempts to automatically resolve a Lancache on the local network. If detected,
     * SteamKit will route all downloads through the cache rather than through Steam's CDN.
     * Will try to detect the Lancache through the poisoned DNS entry for lancache.steamcontent.com
     *
     * This is a modified version from the original source:
     * https://github.com/tpill90/lancache-prefill-common/blob/main/dotnet/LancacheIpResolver.cs
     */
    suspend fun detectLancacheServer() {
        withContext(Dispatchers.IO) {
            try {
                val addresses = InetAddress.getAllByName(TRIGGER_DOMAIN)
                val ipAddresses = addresses.filter { address ->
                    address is Inet4Address || address is Inet6Address
                }

                useLanCacheServer = ipAddresses.any { isPrivateAddress(it) }
            } catch (_: Exception) {
                useLanCacheServer = false
            }
        }
    }

    /**
     * Determines if an IP address is a private address, as specified in RFC1918
     *
     * @param toTest The IP address that will be tested
     * @return Returns true if the IP is a private address, false if it isn't private
     */
    @JvmStatic
    fun isPrivateAddress(toTest: InetAddress): Boolean {
        if (toTest.isLoopbackAddress) {
            return true
        }

        val bytes = toTest.address

        // IPv4
        if (toTest is Inet4Address) {
            // Convert signed byte to unsigned for comparison
            val firstOctet = bytes[0].toInt() and 0xFF

            return when (firstOctet) {
                10 -> true
                172 -> {
                    val secondOctet = bytes[1].toInt() and 0xFF
                    secondOctet in 16..<32
                }

                192 -> {
                    val secondOctet = bytes[1].toInt() and 0xFF
                    secondOctet == 168
                }

                else -> false
            }
        }

        // IPv6
        if (toTest is Inet6Address) {
            // Check for Unique Local Address (fc00::/7) and link-local
            val firstByte = bytes[0].toInt() and 0xFF
            return (firstByte and 0xFE) == 0xFC || toTest.isLinkLocalAddress
        }

        return false
    }

    /**
     * Builds an HTTP request for Lancache with proper headers
     *
     * @param server The server to route the request through
     * @param command The API command/path
     * @param query Optional query parameters
     * @return OkHttp Request object configured for Lancache
     */
    fun buildLancacheRequest(server: Server, command: String, query: String? = null): Request {
        val urlBuilder = "http://lancache.steamcontent.com:80".toHttpUrl().newBuilder()
            .addPathSegments(command.trimStart('/'))

        query?.let { queryString ->
            if (queryString.isNotEmpty()) {
                val params = queryString.split("&")
                for (param in params) {
                    val keyValue = param.split("=", limit = 2)
                    if (keyValue.size == 2) {
                        urlBuilder.addQueryParameter(keyValue[0], keyValue[1])
                    } else if (keyValue.size == 1 && keyValue[0].isNotEmpty()) {
                        urlBuilder.addQueryParameter(keyValue[0], "")
                    }
                }
            }
        }

        return Request.Builder()
            .url(urlBuilder.build())
            .header("Host", server.host ?: "")
            // User agent must match the Steam client in order for Lancache to correctly identify and cache Valve's CDN content
            .header("User-Agent", "Valve/Steam HTTP Client 1.0")
            .build()
    }
}
