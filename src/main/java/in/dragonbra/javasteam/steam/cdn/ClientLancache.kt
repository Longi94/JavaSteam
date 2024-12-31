package `in`.dragonbra.javasteam.steam.cdn

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.future.future
import okhttp3.HttpUrl
import okhttp3.Request
import java.net.Inet4Address
import java.net.Inet6Address
import java.net.InetAddress
import java.util.concurrent.*

@Suppress("unused")
object ClientLancache {

    private const val TRIGGER_DOMAIN: String = "lancache.steamcontent.com"

    /**
     * When set to true, will attempt to download from a Lancache instance on the LAN rather than going out to Steam's CDNs.
     */
    var useLanCacheServer: Boolean = false

    /**
     * Attempts to automatically resolve a Lancache on the local network.
     * If detected, SteamKit will route all downloads through the cache rather than through Steam's CDN.
     * Will try to detect the Lancache through the poisoned DNS entry for lancache.steamcontent.com
     *
     * This is a modified version from the original source :
     * https://github.com/tpill90/lancache-prefill-common/blob/main/dotnet/LancacheIpResolver.cs
     */
    @JvmStatic
    @JvmOverloads
    fun detectLancacheServer(dispatcher: CoroutineDispatcher = Dispatchers.IO): CompletableFuture<Unit> =
        CoroutineScope(dispatcher).future {
            val ipAddresses = InetAddress.getAllByName(TRIGGER_DOMAIN)
                .filter { it is Inet4Address || it is Inet6Address }

            useLanCacheServer = ipAddresses.any { isPrivateAddress(it) }
        }

    /**
     * Determines if an IP address is a private address, as specified in RFC1918
     * @param toTest The IP address that will be tested
     * @return true if the IP is a private address, false if it isn't private
     */
    @JvmStatic
    fun isPrivateAddress(toTest: InetAddress): Boolean {
        if (toTest.isLoopbackAddress) return true
        val bytes = toTest.address
        return when (toTest) {
            is Inet4Address -> when (bytes[0].toInt() and 0xFF) {
                10 -> true
                172 -> (bytes[1].toInt() and 0xFF) in 16..31
                192 -> bytes[1].toInt() and 0xFF == 168
                else -> false
            }
            is Inet6Address -> (bytes[0].toInt() and 0xFE) == 0xFC || toTest.isLinkLocalAddress
            else -> false
        }
    }

    fun buildLancacheRequest(server: Server, command: String, query: String?): Request = Request.Builder()
        .url(
            HttpUrl.Builder()
                .scheme("http")
                .host("lancache.steamcontent.com")
                .port(80)
                .addPathSegments(command)
                .query(query)
                .build()
        )
        .header("Host", server.host)
        .header("User-Agent", "Valve/Steam HTTP Client 1.0")
        .build()
}
