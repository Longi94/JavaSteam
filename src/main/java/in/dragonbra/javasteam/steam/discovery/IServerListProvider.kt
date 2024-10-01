package `in`.dragonbra.javasteam.steam.discovery

/**
 * An interface for persisting the server list for connection discovery
 */
interface IServerListProvider {
    /**
     * Ask a provider to fetch any servers that it has available
     * @return A list of IPEndPoints representing servers
     */
    fun fetchServerList(): List<ServerRecord>

    /**
     * Update the persistent list of endpoints
     * @param endpoints List of endpoints
     */
    fun updateServerList(endpoints: List<ServerRecord>)
}
