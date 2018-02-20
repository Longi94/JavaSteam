package in.dragonbra.javasteam.steam.discovery;

import java.util.Enumeration;

/**
 * An interface for persisting the server list for connection discovery
 */
public interface IServerListProvider {

    /**
     * Ask a provider to fetch any servers that it has available
     *
     * @return A list of IPEndPoints representing servers
     */
    Enumeration<ServerRecord> fetchServerListAsync();

    /**
     * Update the persistent list of endpoints
     *
     * @param endpoints List of endpoints
     */
    void updateServerListAsync(Enumeration<ServerRecord> endpoints);
}
