package in.dragonbra.javasteam.steam.handlers.steamtrading.callback;

import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserver2.CMsgTrading_StartSession;
import in.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackMsg;
import in.dragonbra.javasteam.types.SteamID;

/**
 * This callback is fired when a trading session has started.
 */
public class SessionStartCallback extends CallbackMsg {

    private SteamID otherClient;

    public SessionStartCallback(CMsgTrading_StartSession.Builder msg) {
        otherClient = new SteamID(msg.getOtherSteamid());
    }

    public SteamID getOtherClient() {
        return otherClient;
    }
}
