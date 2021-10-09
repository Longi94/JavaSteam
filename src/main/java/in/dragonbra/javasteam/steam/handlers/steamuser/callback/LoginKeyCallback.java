package in.dragonbra.javasteam.steam.handlers.steamuser.callback;

import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserverLogin.CMsgClientNewLoginKey;
import in.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackMsg;

/**
 * This callback is returned some time after logging onto the network.
 */
public class LoginKeyCallback extends CallbackMsg {

    private String loginKey;

    private int uniqueID;

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
