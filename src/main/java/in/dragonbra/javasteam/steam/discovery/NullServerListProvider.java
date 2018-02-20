package in.dragonbra.javasteam.steam.discovery;

import java.util.Enumeration;

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
    public void updateServerListAsync(Enumeration<ServerRecord> endpoints) {

    }
}
