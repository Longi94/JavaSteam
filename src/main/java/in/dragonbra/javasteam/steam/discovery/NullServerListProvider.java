package in.dragonbra.javasteam.steam.discovery;

import java.util.Enumeration;
import java.util.List;

/**
 * @author lngtr
 * @since 2018-02-20
 */
public class NullServerListProvider implements IServerListProvider {
    @Override
    public Enumeration<ServerRecord> fetchServerListAsync() {
        return null;
    }

    @Override
    public void updateServerList(List<ServerRecord> endpoints) {

    }
}
