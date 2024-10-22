package `in`.dragonbra.javasteam.steam.discovery

import java.time.Instant

/**
 * An interface for persisting the server list for connection discovery
 */
interface IServerListProvider {

    /**
     * When the server list was last refreshed, used to determine if the server list should be refreshed from the Steam Directory
     * This should return DateTime with the UTC kind
     */
    val lastServerListRefresh: Instant

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
