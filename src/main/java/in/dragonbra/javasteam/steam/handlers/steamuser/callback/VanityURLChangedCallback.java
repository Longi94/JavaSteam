package in.dragonbra.javasteam.steam.handlers.steamuser.callback;

import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserver2;
import in.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackMsg;
import in.dragonbra.javasteam.types.JobID;

/**
 * This callback is received when users' vanity url changes.
 */
public class VanityURLChangedCallback extends CallbackMsg {

    private String vanityUrl;

    public VanityURLChangedCallback(JobID jobID, SteammessagesClientserver2.CMsgClientVanityURLChangedNotification.Builder msg) {
        setJobID(jobID);

        vanityUrl = msg.getVanityUrl();
    }

    /**
     * @return the new vanity url.
     */
    public String getVanityUrl() {
        return vanityUrl;
    }
}
