package in.dragonbra.javasteam.steam.webapi;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;
import in.dragonbra.javasteam.enums.EResult;
import in.dragonbra.javasteam.enums.deserializer.EResultDeserializer;
import in.dragonbra.javasteam.steam.discovery.ServerRecord;
import in.dragonbra.javasteam.steam.steamclient.configuration.SteamConfiguration;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

/**
 * Helper class to load servers from the Steam Directory Web API.
 */
public class SteamDirectory {

    private static final OkHttpClient client = new OkHttpClient();

    private static final Gson gson;

    static {
        gson = new GsonBuilder()
                .registerTypeAdapter(EResult.class, new EResultDeserializer())
                .create();
    }

    public static List<ServerRecord> load(SteamConfiguration configuration) throws IOException {
        if (configuration == null) {
            throw new IllegalArgumentException("configuration null");
        }

        String baseAddress = configuration.getWebAPIBaseAddress();

        String url = baseAddress + "ISteamDirectory/GetCMList/v1?cellid=" + String.valueOf(configuration.getCellID());

        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        Response response = client.newCall(request).execute();

        CMListResponseWrapper cmListResponseWrapper = gson.fromJson(response.body().string(), CMListResponseWrapper.class);

        CMListResponse cmListResponse = cmListResponseWrapper.getResponse();

        if (cmListResponse.getResult() != EResult.OK) {
            throw new UnsupportedOperationException("Steam Web API return EResult." + cmListResponse.getResult());
        }

        List<String> socketList = cmListResponse.getServerList();
        List<String> webSocketList = cmListResponse.getWebSocketList();

        List<ServerRecord> records = new ArrayList<>();

        socketList.forEach(socket -> {
            String[] split = socket.split(":");
            records.add(ServerRecord.createSocketServer(new InetSocketAddress(split[0], Integer.parseInt(split[1]))));
        });

        webSocketList.forEach(socket -> {
            records.add(ServerRecord.createWebSocketServer(socket));
        });

        return records;
    }

    public static class CMListResponse {
        private EResult result;

        private String message;

        @SerializedName("serverlist")
        private List<String> serverList;

        @SerializedName("serverlist_websockets")
        private List<String> webSocketList;

        public EResult getResult() {
            return result;
        }

        public void setResult(EResult result) {
            this.result = result;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public List<String> getServerList() {
            return serverList;
        }

        public void setServerList(List<String> serverList) {
            this.serverList = serverList;
        }

        public List<String> getWebSocketList() {
            return webSocketList;
        }

        public void setWebSocketList(List<String> webSocketList) {
            this.webSocketList = webSocketList;
        }
    }

    public static class CMListResponseWrapper {
        private CMListResponse response;

        public CMListResponse getResponse() {
            return response;
        }

        public void setResponse(CMListResponse response) {
            this.response = response;
        }
    }
}
