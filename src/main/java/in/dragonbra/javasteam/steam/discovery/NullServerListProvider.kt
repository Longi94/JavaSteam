package in.dragonbra.javasteam.steam.discovery;

import java.util.List;

/**
 * @author lngtr
 * @since 2018-02-20
 */
public class NullServerListProvider implements IServerListProvider {
    @Override
    public List<ServerRecord> fetchServerList() {
        return null;
    }

    @Override
    public void updateServerList(List<ServerRecord> endpoints) {

    }
}
