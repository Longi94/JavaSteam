package in.dragonbra.javasteam.steam.handlers.steammatchmaking.callback;

import in.dragonbra.javasteam.enums.EResult;
import in.dragonbra.javasteam.steam.handlers.steammatchmaking.Lobby;
import in.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackMsg;
import in.dragonbra.javasteam.types.JobID;

import java.util.List;

/**
 * This callback is fired in response to
 * {@link in.dragonbra.javasteam.steam.handlers.steammatchmaking.SteamMatchmaking#getLobbyList}.
 *
 * @author lossy
 * @since 2022-06-21
 */
public class GetLobbyListCallback extends CallbackMsg {

    /**
     * ID of the app the lobbies belongs to.
     */
    private int appId;

    /**
     * The result of the request.
     */
    private EResult result;

    /**
     * The list of lobbies matching the criteria specified with
     * {@link in.dragonbra.javasteam.steam.handlers.steammatchmaking.SteamMatchmaking#getLobbyList}.
     */
    private List<Lobby> lobbies;

    public GetLobbyListCallback(JobID jobID, int appId, EResult result, List<Lobby> lobbies) {
        this.setJobID(jobID);
        this.appId = appId;
        this.result = result;
        this.lobbies = lobbies;
    }

    public int getAppId() {
        return appId;
    }

    public EResult getResult() {
        return result;
    }

    public List<Lobby> getLobbies() {
        return lobbies;
    }
}
