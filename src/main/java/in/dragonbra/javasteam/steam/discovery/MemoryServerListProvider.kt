package `in`.dragonbra.javasteam.steam.discovery

/**
 * A server list provider that uses an in-memory list
 */
class MemoryServerListProvider : IServerListProvider {

    var servers: List<ServerRecord> = listOf()

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
    }
}
