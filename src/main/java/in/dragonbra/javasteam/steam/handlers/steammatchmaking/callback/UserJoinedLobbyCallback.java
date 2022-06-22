package in.dragonbra.javasteam.steam.handlers.steammatchmaking.callback;

import in.dragonbra.javasteam.steam.handlers.steammatchmaking.Lobby;
import in.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackMsg;
import in.dragonbra.javasteam.types.SteamID;

/**
 * This callback is fired whenever Steam informs us a user has joined a lobby.
 *
 * @author lossy
 * @since 2022-06-21
 */
public class UserJoinedLobbyCallback extends CallbackMsg {

    /**
     * ID of the app the lobby belongs to.
     */
    private int appId;

    /**
     * The SteamID of the lobby that a member joined.
     */
    private SteamID lobbySteamID;

    /**
     * The lobby member that joined.
     */
    private Lobby.Member user;

    public UserJoinedLobbyCallback(int appId, long lobbySteamID, Lobby.Member user) {
        this.appId = appId;
        this.lobbySteamID = new SteamID(lobbySteamID);
        this.user = user;
    }

    public int getAppId() {
        return appId;
    }

    public SteamID getLobbySteamID() {
        return lobbySteamID;
    }

    public Lobby.Member getUser() {
        return user;
    }
}
