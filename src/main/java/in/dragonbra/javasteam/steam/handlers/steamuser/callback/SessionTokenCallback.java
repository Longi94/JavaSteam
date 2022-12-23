package in.dragonbra.javasteam.steam.handlers.steamuser.callback;

import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserver;
import in.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackMsg;

/**
 * This callback is fired when the client receives it's unique Steam3 session token.
 * This token is used for authenticated content downloading in Steam2.
 */
public class SessionTokenCallback extends CallbackMsg {

    private final long sessionToken;

    public SessionTokenCallback(SteammessagesClientserver.CMsgClientSessionToken.Builder msg) {
        sessionToken = msg.getToken();
    }

    /**
     * @return the Steam3 session token used for authenticating to various other services.
     */
    public long getSessionToken() {
        return sessionToken;
    }
}
