package in.dragonbra.javasteam.steam.handlers.steammatchmaking.callback;

import in.dragonbra.javasteam.enums.EResult;
import in.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackMsg;
import in.dragonbra.javasteam.types.JobID;
import in.dragonbra.javasteam.types.SteamID;

/**
 * This callback is fired in response to
 * {@link in.dragonbra.javasteam.steam.handlers.steammatchmaking.SteamMatchmaking#leaveLobby}
 *
 * @author lossy
 * @since 2022-06-21
 */
public class LeaveLobbyCallback extends CallbackMsg {

    /**
     * ID of the app the targeted lobby belongs to.
     */
    private int appId;

    /**
     * The result of the request.
     */
    private EResult result;

    /**
     * The SteamID of the targeted Lobby.
     */
    private SteamID lobbySteamID;

    public LeaveLobbyCallback(JobID jobID, int appId, int result, long lobbySteamID) {
        this.setJobID(jobID);
        this.appId = appId;
        this.result = EResult.from(result);
        this.lobbySteamID = new SteamID(lobbySteamID);
    }

    public int getAppId() {
        return appId;
    }

    public EResult getResult() {
        return result;
    }

    public SteamID getLobbySteamID() {
        return lobbySteamID;
    }
}
