package in.dragonbra.javasteam.steam.webapi;

import in.dragonbra.javasteam.enums.EResult;
import in.dragonbra.javasteam.steam.discovery.ServerRecord;
import in.dragonbra.javasteam.steam.steamclient.configuration.SteamConfiguration;
import in.dragonbra.javasteam.types.KeyValue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Helper class to load servers from the Steam Directory Web API.
 */
public class SteamDirectory {

    /**
     * Load a list of servers from the Steam Directory.
     *
     * @param configuration Configuration Object
     * @return the list of servers
     * @throws IOException if the request could not be executed
     */
    public static List<ServerRecord> load(SteamConfiguration configuration) throws IOException {
        return load(configuration, -1);
    }

    /**
     * Load a list of servers from the Steam Directory.
     *
     * @param configuration Configuration Object
     * @param maxServers    Max number of servers to return. The API will typically return this number per server type
     *                      (socket and websocket). If negative, the parameter is not added to the request
     * @return the list of servers
     * @throws IOException if the request could not be executed
     */
    public static List<ServerRecord> load(SteamConfiguration configuration, int maxServers) throws IOException {
        if (configuration == null) {
            throw new IllegalArgumentException("configuration null");
        }

        WebAPI api = configuration.getWebAPI("ISteamDirectory");

        Map<String, String> params = new HashMap<>();
        params.put("cellid", String.valueOf(configuration.getCellID()));

        if (maxServers >= 0) {
            params.put("maxcount", String.valueOf(maxServers));
        }

        KeyValue response = api.call("GetCMList", params);

        EResult result = EResult.from(response.get("result").asInteger(EResult.Invalid.code()));

        if (result != EResult.OK) {
            throw new IllegalStateException("Steam Web API returned EResult." + result);
        }

        KeyValue socketList = response.get("serverlist");
        KeyValue webSocketList = response.get("serverlist_websockets");

        List<ServerRecord> records = new ArrayList<>();

        for (KeyValue socket : socketList.getChildren()) {
            ServerRecord record = ServerRecord.tryCreateSocketServer(socket.getValue());
            if (record != null) {
                records.add(record);
            }
        }

        for (KeyValue socket : webSocketList.getChildren()) {
            records.add(ServerRecord.createWebSocketServer(socket.getValue()));
        }

        return records;
    }
}
