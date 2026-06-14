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

    /**
     * Releases all resources held by this pool. Clears the server list and nulls out the CDN client.
     * After closing, [getConnection] will throw [IllegalStateException].
     */
    override fun close() {
        logger?.debug("Closing...")

        servers.set(emptyList())

        cdnClient = null
        proxyServer = null

        logger = null
    }

    /**
     * Fetches the current CDN server list from Steam and resets the round-robin index.
     * Servers are filtered to those eligible for [appId] and sorted by weighted load.
     * Must be called before [getConnection]. Throws if no servers are returned.
     * @param maxNumServers Optional cap on the number of servers to request. Null requests the default amount.
     * @throws Exception if Steam returns an empty server list.
     */
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

    /** Returns true if the pool contains at least one server. */
    fun hasServers(): Boolean = servers.get().isNotEmpty()

    /**
     * Returns the next server in round-robin order.
     * @throws IllegalStateException if the server list is empty.
     */
    fun getConnection(): Server {
        val servers = servers.get()

        if (servers.isEmpty()) throw IllegalStateException("No CDN servers available")

        val index = nextServer.getAndIncrement()
        val server = servers[index % servers.size]

        logger?.debug("Getting connection $server")

        return server
    }

    /**
     * Returns a successfully used [server] to the pool.
     * Call this after a chunk or manifest download completes without error.
     */
    fun returnConnection(server: Server?) {
        if (server == null) {
            logger?.error("null server returned to cdn pool.")
            return
        }

        logger?.debug("Returning connection: $server")

        // (SK) nothing to do, maybe remove from ContentServerPenalty?
    }

    /**
     * Transiently skips [server] by advancing the round-robin index.
     * Use for recoverable failures (HTTP 5xx, timeouts) where the server may succeed later.
     */
    fun skipConnection(server: Server?) {
        if (server == null) return

        logger?.debug("Skipping connection: $server")

        nextServer.incrementAndGet()
    }

    /**
     * Permanently removes [server] from the pool.
     * Use only for unrecoverable failures (e.g. DNS resolution failure) where the host is unreachable.
     */
    fun returnBrokenConnection(server: Server?) {
        if (server == null) {
            logger?.error("null broken server returned to pool")
            return
        }

        logger?.debug("Returning broken connection: $server")

        val updated = servers.updateAndGet { it.filter { s -> s != server } }
        if (updated.isEmpty()) {
            logger?.error("No CDN servers remaining after removing broken connection: $server")
        }

        // TODO: (SK) Add server to ContentServerPenalty
    }
}
