package in.dragonbra.javasteam.steam.handlers.steammatchmaking.callback;

import in.dragonbra.javasteam.enums.EChatRoomEnterResponse;
import in.dragonbra.javasteam.steam.handlers.steammatchmaking.Lobby;
import in.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackMsg;
import in.dragonbra.javasteam.types.JobID;

/**
 * This callback is fired in response to
 * {@link in.dragonbra.javasteam.steam.handlers.steammatchmaking.SteamMatchmaking#joinLobby}.
 *
 * @author lossy
 * @since 2022-06-21
 */
public class JoinLobbyCallback extends CallbackMsg {

    /**
     * ID of the app the targeted lobby belongs to.
     */
    private int appId;

    /**
     * The result of the request.
     */
    private EChatRoomEnterResponse chatRoomEnterResponse;

    /**
     * The joined {@link Lobby}, when {@link JoinLobbyCallback#chatRoomEnterResponse} equals
     * <see cref="EChatRoomEnterResponse.Success"/>, otherwise <c>null</c>
     */
    private Lobby lobby;

    public JoinLobbyCallback(JobID jobID, int appId, EChatRoomEnterResponse response, Lobby lobby) {
        this.setJobID(jobID);
        this.appId = appId;
        this.chatRoomEnterResponse = response;
        this.lobby = lobby;
    }


    public int getAppId() {
        return appId;
    }

    public EChatRoomEnterResponse getChatRoomEnterResponse() {
        return chatRoomEnterResponse;
    }

    public Lobby getLobby() {
        return lobby;
    }
}
