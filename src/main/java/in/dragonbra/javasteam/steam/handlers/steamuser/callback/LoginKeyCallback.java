package in.dragonbra.javasteam.steam.handlers.steamuser.callback;

import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserverLogin.CMsgClientNewLoginKey;
import in.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackMsg;

/**
 * This callback is returned some time after logging onto the network.
 *
 * @deprecated Steam no longer sends new login keys as of March 2023, use SteamAuthentication.
 */
@Deprecated
public class LoginKeyCallback extends CallbackMsg {

    private final String loginKey;

    private final int uniqueID;

    public LoginKeyCallback(CMsgClientNewLoginKey.Builder logKey) {
        this.loginKey = logKey.getLoginKey();
        this.uniqueID = logKey.getUniqueId();
    }

    /**
     * @return the login key.
     */
    public String getLoginKey() {
        return loginKey;
    }

    /**
     * @return the unique ID.
     */
    public int getUniqueID() {
        return uniqueID;
    }
}
