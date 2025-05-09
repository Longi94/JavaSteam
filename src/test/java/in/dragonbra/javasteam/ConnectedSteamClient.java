package in.dragonbra.javasteam;

import in.dragonbra.javasteam.steam.steamclient.SteamClient;

public class ConnectedSteamClient {
    public static SteamClient get() {
        var client = new SteamClient();
        client.setIsConnected(true);

        return client;
    }
}
