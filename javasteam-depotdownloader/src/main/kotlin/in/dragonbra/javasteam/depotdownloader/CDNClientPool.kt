package `in`.dragonbra.javasteam.depotdownloader

import `in`.dragonbra.javasteam.steam.cdn.Client
import `in`.dragonbra.javasteam.steam.cdn.Server
import `in`.dragonbra.javasteam.util.log.LogManager
import `in`.dragonbra.javasteam.util.log.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference

/**
 * Manages a pool of CDN server connections for efficient content downloading.
 * This class maintains a list of available CDN servers, automatically selects appropriate
 * servers based on load and app compatibility, and handles connection rotation when
 * servers fail or become unavailable.
 *
 * @param steamSession The Steam3 session for server communication
 * @param appId The application ID to download - used to filter compatible CDN servers
 * @param scope The coroutine scope for async operations
 * @param debug If true, enables debug logging
 *
 * @author Oxters
 * @author Lossy
 * @since Nov 7, 2024
 */
class CDNClientPool(
    private val steamSession: Steam3Session,
    private val appId: Int,
    private val scope: CoroutineScope,
    debug: Boolean = false,
) : AutoCloseable {

    private var logger: Logger? = null

    private val servers = AtomicReference<List<Server>>(emptyList())

    private var nextServer: AtomicInteger = AtomicInteger(0)

    private val mutex: Mutex = Mutex()

    var cdnClient: Client? = null
        private set

    var proxyServer: Server? = null
        private set

    init {
        cdnClient = Client(steamSession.steamClient)

        if (debug) {
            logger = LogManager.getLogger(CDNClientPool::class.java)
        }
    }

    override fun close() {
        logger?.debug("Closing...")

        servers.set(emptyList())

        cdnClient = null
        proxyServer = null

        logger = null
    }

    @Throws(Exception::class)
    suspend fun updateServerList(maxNumServers: Int? = null) = mutex.withLock {
        val serversForSteamPipe = steamSession.steamContent!!.getServersForSteamPipe(
            cellId = steamSession.steamClient.cellID ?: 0,
            maxNumServers = maxNumServers,
            parentScope = scope
        ).await()

        proxyServer = serversForSteamPipe.firstOrNull { it.useAsProxy }

        val weightedCdnServers = serversForSteamPipe
            .filter { server ->
                val isEligibleForApp = server.allowedAppIds.isEmpty() || server.allowedAppIds.contains(appId)
                isEligibleForApp && (server.type == "SteamCache" || server.type == "CDN")
            }
            .sortedBy { it.weightedLoad }

        // ContentServerPenalty removed for now.

        servers.set(weightedCdnServers)

        nextServer.set(0)

        // servers.joinToString(separator = "\n", prefix = "Servers:\n") { "- $it" }
        logger?.debug("Found ${weightedCdnServers.size} Servers")

        if (weightedCdnServers.isEmpty()) {
            throw Exception("Failed to retrieve any download servers.")
        }
    }

    fun getConnection(): Server {
        val servers = servers.get()

        val index = nextServer.getAndIncrement()
        val server = servers[index % servers.size]

        logger?.debug("Getting connection $server")

        return server
    }

    fun returnConnection(server: Server?) {
        if (server == null) {
            logger?.error("null server returned to cdn pool.")
            return
        }

        logger?.debug("Returning connection: $server")

        // (SK) nothing to do, maybe remove from ContentServerPenalty?
    }

    fun returnBrokenConnection(server: Server?) {
        if (server == null) {
            logger?.error("null broken server returned to pool")
            return
        }

        logger?.debug("Returning broken connection: $server")

        val servers = servers.get()
        val currentIndex = nextServer.get()

        if (servers.isNotEmpty() && servers[currentIndex % servers.size] == server) {
            nextServer.incrementAndGet()

            // TODO: (SK) Add server to ContentServerPenalty
        }
    }
}
