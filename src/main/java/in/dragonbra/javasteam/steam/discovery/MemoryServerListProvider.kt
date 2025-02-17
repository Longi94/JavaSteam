package `in`.dragonbra.javasteam.steam.discovery

import java.time.Instant

/**
 * A server list provider that uses an in-memory list
 */
class MemoryServerListProvider : IServerListProvider {

    private var servers: List<ServerRecord> = listOf()

    private var lastUpdated: Instant = Instant.MIN

    /**
     * Returns the last time the server list was updated
     */
    override val lastServerListRefresh: Instant
        get() = lastUpdated

    /**
     * Returns the stored server list in memory
     * @return List of servers if persisted, otherwise an empty list
     */
    override fun fetchServerList(): List<ServerRecord> = servers

    /**
     * Stores the supplied list of servers in memory
     * @param endpoints List of endpoints (servers)
     */
    override fun updateServerList(endpoints: List<ServerRecord>) {
        servers = endpoints
        lastUpdated = Instant.now()
    }
}
