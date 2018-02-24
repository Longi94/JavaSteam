package in.dragonbra.javasteam.steam.handlers.steamuser.callback;

import in.dragonbra.javasteam.enums.EResult;
import in.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackMsg;

/**
 * This callback is returned when the client is told to log off by the server.
 */
public class LoggedOffCallback extends CallbackMsg {

    private EResult result;

    public LoggedOffCallback(EResult result) {
        this.result = result;
    }

    public EResult getResult() {
        return result;
    }
}
