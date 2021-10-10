package in.dragonbra.javasteam.steam.handlers.steamfriends.callback;

import in.dragonbra.javasteam.enums.EResult;
import in.dragonbra.javasteam.generated.MsgClientSetIgnoreFriendResponse;
import in.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackMsg;
import in.dragonbra.javasteam.types.JobID;

/**
 * This callback is fired in response to an attempt at ignoring a friend.
 */
public class IgnoreFriendCallback extends CallbackMsg {

    private EResult result;

    public IgnoreFriendCallback(JobID jobID, MsgClientSetIgnoreFriendResponse response) {
        setJobID(jobID);
        result = response.getResult();
    }

    /**
     * @return the result of ignoring a friend.
     */
    public EResult getResult() {
        return result;
    }
}
