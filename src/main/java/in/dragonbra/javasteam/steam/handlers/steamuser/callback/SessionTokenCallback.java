package in.dragonbra.javasteam.steam.handlers.steamuser.callback;

import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserver;
import in.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackMsg;

/**
 * This callback is fired when the client recieves it's unique Steam3 session token. This token is used for authenticated content downloading in Steam2.
 */
public class SessionTokenCallback extends CallbackMsg {

    private long sessionToken;

    public SessionTokenCallback(SteammessagesClientserver.CMsgClientSessionToken.Builder msg) {
        sessionToken = msg.getToken();
    }

    public long getSessionToken() {
        return sessionToken;
    }
}
