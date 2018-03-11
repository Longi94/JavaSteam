package in.dragonbra.javasteam.steam.handlers.steamgameserver.callback;

import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserver.CMsgGSStatusReply;
import in.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackMsg;

/**
 * This callback is fired when the game server receives a status reply.
 */
public class StatusReplyCallback extends CallbackMsg {
    private boolean secure;

    public StatusReplyCallback(CMsgGSStatusReply.Builder reply) {
        secure = reply.getIsSecure();
    }

    /**
     * @return <b>true</b> if this server is VAC secure; otherwise, <b>false</b>.
     */
    public boolean isSecure() {
        return secure;
    }
}
