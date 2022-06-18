package in.dragonbra.javasteam.steam.handlers.steamapps.callback;

import in.dragonbra.javasteam.enums.EResult;
import in.dragonbra.javasteam.generated.MsgClientGetLegacyGameKeyResponse;
import in.dragonbra.javasteam.steam.handlers.steamapps.SteamApps;
import in.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackMsg;
import in.dragonbra.javasteam.types.JobID;

/**
 * This callback is received in response to calling {@link SteamApps#getLegacyGameKey}.
 */
public class LegacyGameKeyCallback extends CallbackMsg {

    private EResult result;

    private int appID;

    private String key;

    public LegacyGameKeyCallback(JobID jobID, MsgClientGetLegacyGameKeyResponse msg, byte[] payload) {
        setJobID(jobID);
        this.appID = msg.getAppId();
        this.result = msg.getResult();

        if (msg.getLength() > 0) {
            int length = msg.getLength() - 1;
            key = new String(payload, 0, length);
        }
    }

    /**
     * @return the result of requesting this game key.
     */
    public EResult getResult() {
        return result;
    }

    /**
     * @return the appid that this game key is for.
     */
    public int getAppID() {
        return appID;
    }

    /**
     * @return the game key.
     */
    public String getKey() {
        return key;
    }
}
