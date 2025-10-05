package `in`.dragonbra.javasteam.depotdownloader

import `in`.dragonbra.javasteam.steam.cdn.Client
import `in`.dragonbra.javasteam.steam.cdn.Server
import `in`.dragonbra.javasteam.util.log.LogManager
import `in`.dragonbra.javasteam.util.log.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

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

    private val servers: ArrayList<Server> = arrayListOf()

    private var nextServer: Int = 0

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
        servers.clear()

        cdnClient = null
        proxyServer = null

        logger = null
    }

    @Throws(Exception::class)
    suspend fun updateServerList(maxNumServers: Int? = null) = mutex.withLock {
        if (servers.isNotEmpty()) {
            servers.clear()
        }

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

        servers.addAll(weightedCdnServers)

        // servers.joinToString(separator = "\n", prefix = "Servers:\n") { "- $it" }
        logger?.debug("Found ${servers.size} Servers")

        if (servers.isEmpty()) {
            throw Exception("Failed to retrieve any download servers.")
        }
    }

    suspend fun getConnection(): Server = mutex.withLock {
        val server = servers[nextServer % servers.count()]

        logger?.debug("Getting connection $server")

        return server
    }

    suspend fun returnConnection(server: Server?) = mutex.withLock {
        if (server == null) {
            return@withLock
        }

        logger?.debug("Returning connection: $server")

        // (SK) nothing to do, maybe remove from ContentServerPenalty?
    }

    suspend fun returnBrokenConnection(server: Server?) = mutex.withLock {
        if (server == null) {
            return@withLock
        }

        logger?.debug("Returning broken connection: $server")

        if (servers[nextServer % servers.count()] == server) {
            nextServer++

            // TODO: (SK) Add server to ContentServerPenalty
        }
    }
}
