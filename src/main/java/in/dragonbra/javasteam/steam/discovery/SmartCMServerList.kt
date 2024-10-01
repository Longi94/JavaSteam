package `in`.dragonbra.javasteam.steam.discovery

import `in`.dragonbra.javasteam.networking.steam3.ProtocolTypes
import `in`.dragonbra.javasteam.steam.steamclient.configuration.SteamConfiguration
import `in`.dragonbra.javasteam.steam.webapi.SteamDirectory
import `in`.dragonbra.javasteam.util.log.LogManager
import `in`.dragonbra.javasteam.util.log.Logger
import java.io.IOException
import java.net.InetSocketAddress
import java.time.Duration
import java.time.Instant
import java.util.EnumSet

/**
 * Smart list of CM servers.
 */
@Suppress("unused")
class SmartCMServerList(private val configuration: SteamConfiguration) {

    private val servers: MutableList<ServerInfo> = mutableListOf()

    // Determines how long a server's bad connection state is remembered for.
    var badConnectionMemoryTimeSpan: Duration = Duration.ofMinutes(5)

    @Throws(IOException::class)
    private fun startFetchingServers() {
        // if the server list has been populated, no need to perform any additional work
        if (!servers.isEmpty()) {
            return
        }

        resolveServerList()
    }

    @Throws(IOException::class)
    private fun resolveServerList() {
        logger.debug("Resolving server list")

        val serverList = configuration.serverListProvider.fetchServerList()
        var endpointList = serverList.toList()

        if (endpointList.isEmpty() && configuration.isAllowDirectoryFetch) {
            logger.debug("Server list provider had no entries, will query SteamDirectory")
            endpointList = SteamDirectory.load(configuration)
        }

        if (endpointList.isEmpty() && configuration.isAllowDirectoryFetch) {
            logger.debug("Could not query SteamDirectory, falling back to cm2-ord1")

            // Grabbed a random host that is not an IP address from the endpoint list.
            val cm0 = InetSocketAddress("cm2-ord1.cm.steampowered.com", 27017)
            endpointList = listOf(ServerRecord.createSocketServer(cm0))
        }

        logger.debug("Resolved ${endpointList.size} servers")
        replaceList(endpointList)
    }

    /**
     * Resets the scores of all servers which has a last bad connection more than [SmartCMServerList.badConnectionMemoryTimeSpan] ago.
     */
    fun resetOldScores() {
        val cutoff = Instant.now().minus(badConnectionMemoryTimeSpan)

        servers.forEach { serverInfo ->
            serverInfo.lastBadConnectionTimeUtc?.let { lastConnectionTime ->
                if (lastConnectionTime.isBefore(cutoff)) {
                    serverInfo.lastBadConnectionTimeUtc = null
                }
            }
        }
    }

    /**
     * Replace the list with a new list of servers provided to us by the Steam servers.
     *
     * @param endpointList The [ServerRecord] to use for this [SmartCMServerList].
     */
    fun replaceList(endpointList: List<ServerRecord>) {
        val distinctEndPoints = endpointList.distinct()

        servers.clear()

        distinctEndPoints.forEach(::addCore)

        configuration.serverListProvider.updateServerList(distinctEndPoints)
    }

    private fun addCore(endPoint: ServerRecord) {
        endPoint.protocolTypes.forEach { protocolType ->
            val info = ServerInfo(endPoint, protocolType)
            servers.add(info)
        }
    }

    /**
     * Explicitly resets the known state of all servers.
     */
    fun resetBadServers() {
        servers.forEach { serverInfo ->
            serverInfo.lastBadConnectionTimeUtc = null
        }
    }

    fun tryMark(endPoint: InetSocketAddress, protocolTypes: ProtocolTypes, quality: ServerQuality): Boolean =
        tryMark(endPoint, EnumSet.of(protocolTypes), quality)

    fun tryMark(endPoint: InetSocketAddress, protocolTypes: EnumSet<ProtocolTypes>, quality: ServerQuality): Boolean {
        var serverInfos = listOf<ServerInfo>()

        if (quality == ServerQuality.GOOD) {
            serverInfos = servers.filter { x ->
                x.record.endpoint == endPoint && protocolTypes.contains(x.protocol)
            }
        } else {
            // If we're marking this server for any failure, mark all endpoints for the host at the same time
            val host = endPoint.hostString
            serverInfos = servers.filter { x ->
                x.record.host == host
            }
        }

        if (serverInfos.isEmpty()) {
            return false
        }

        for (serverInfo in serverInfos) {
            logger.debug("Marking ${serverInfo.record.endpoint} - ${serverInfo.protocol} as $quality")
            markServerCore(serverInfo, quality)
        }

        return true
    }

    private fun markServerCore(serverInfo: ServerInfo, quality: ServerQuality) {
        when (quality) {
            ServerQuality.GOOD -> serverInfo.lastBadConnectionTimeUtc = null
            ServerQuality.BAD -> serverInfo.lastBadConnectionTimeUtc = Instant.now()
        }
    }

    /**
     * Perform the actual score lookup of the server list and return the candidate.
     *
     * @param supportedProtocolTypes The minimum supported [ProtocolTypes] of the server to return.
     * @return An [ServerRecord], or null if the list is empty.
     */
    private fun getNextServerCandidateInternal(supportedProtocolTypes: EnumSet<ProtocolTypes>): ServerRecord? {
        resetOldScores()

        val result = servers
            .asSequence()
            .filter { supportedProtocolTypes.contains(it.protocol) }
            .mapIndexed { index, server -> server to index }
            .sortedWith(compareBy({ it.first.lastBadConnectionTimeUtc ?: Instant.EPOCH }, { it.second }))
            .map { it.first }
            .firstOrNull()

        if (result == null) {
            return null
        }

        logger.debug("Next server candidate: ${result.record.endpoint} (${result.protocol})")
        return ServerRecord(result.record.endpoint, result.protocol)
    }

    /**
     * Get the next server in the list.
     *
     * @param supportedProtocolTypes The minimum supported [ProtocolTypes] of the server to return.
     * @return An [ServerRecord], or null if the list is empty.
     */
    fun getNextServerCandidate(supportedProtocolTypes: EnumSet<ProtocolTypes>): ServerRecord? {
        try {
            startFetchingServers()
        } catch (e: IOException) {
            return null
        }

        return getNextServerCandidateInternal(supportedProtocolTypes)
    }

    /**
     * Get the next server in the list.
     *
     * @param supportedProtocolTypes The minimum supported [ProtocolTypes] of the server to return.
     * @return An [ServerRecord], or null if the list is empty.
     */
    fun getNextServerCandidate(supportedProtocolTypes: ProtocolTypes): ServerRecord? =
        getNextServerCandidate(EnumSet.of(supportedProtocolTypes))

    /**
     * Gets the [ServerRecords][ServerRecord] of all servers in the server list.
     *
     * @return An [List] array contains the [ServerRecords][ServerRecord] of the servers in the list
     */
    fun getAllEndPoints(): List<ServerRecord?> {
        runCatching {
            startFetchingServers()
        }.onFailure { return emptyList() }

        val endPoints = servers.map { s -> s.record }.distinct()

        return endPoints
    }

    companion object {
        private val logger: Logger = LogManager.getLogger(SmartCMServerList::class.java)
    }
}
