package in.dragonbra.javasteam.steam.handlers.steammatchmaking.callback;

import in.dragonbra.javasteam.steam.handlers.steammatchmaking.Lobby;
import in.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackMsg;
import in.dragonbra.javasteam.types.JobID;

/**
 * This callback is fired in response to
 * {@link in.dragonbra.javasteam.steam.handlers.steammatchmaking.SteamMatchmaking#getLobbyData},
 * as well as whenever Steam sends us updated lobby data.
 *
 * @author lossy
 * @since 2022-06-21
 */
public class LobbyDataCallback extends CallbackMsg {

    /**
     * ID of the app the updated lobby belongs to.
     */
    private int appId;

    /**
     * The lobby that was updated.
     */
    private Lobby lobby;

    public LobbyDataCallback(JobID jobID, int appId, Lobby lobby) {
        this.setJobID(jobID);
        this.appId = appId;
        this.lobby = lobby;
    }

    public int getAppId() {
        return appId;
    }

    public Lobby getLobby() {
        return lobby;
    }
}
