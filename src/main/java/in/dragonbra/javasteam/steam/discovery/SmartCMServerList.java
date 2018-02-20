package in.dragonbra.javasteam.steam.discovery;

import in.dragonbra.javasteam.steam.steamclient.configuration.SteamConfiguration;

/**
 * @author lngtr
 * @since 2018-02-20
 */
public class SmartCMServerList {

    public SmartCMServerList(SteamConfiguration steamConfiguration) {
        if (steamConfiguration == null) {
            throw new IllegalArgumentException("configuration is null");
        }
    }
}
