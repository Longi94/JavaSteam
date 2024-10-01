package in.dragonbra.javasteam.steam.discovery;

import java.util.ArrayList;
import java.util.List;

/**
 * A server list provider that uses an in-memory list
 */
public class MemoryServerListProvider implements IServerListProvider {

    List<ServerRecord> _server = new ArrayList<>();

    /**
     * Returns the stored server list in memory
     *
     * @return List of servers if persisted, otherwise an empty list
     */
    @Override
    public List<ServerRecord> fetchServerList() {
        return _server;
    }

    /**
     * Stores the supplied list of servers in memory
     *
     * @param endpoints List of endpoints (servers)
     */
    @Override
    public void updateServerList(List<ServerRecord> endpoints) {
        _server = endpoints;
    }
}
