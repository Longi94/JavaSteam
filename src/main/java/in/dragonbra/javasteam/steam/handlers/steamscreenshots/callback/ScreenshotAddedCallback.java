package in.dragonbra.javasteam.steam.handlers.steamscreenshots.callback;

import in.dragonbra.javasteam.enums.EResult;
import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserver2.CMsgClientUCMAddScreenshotResponse;
import in.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackMsg;
import in.dragonbra.javasteam.types.JobID;
import in.dragonbra.javasteam.types.UGCHandle;

/**
 * This callback is fired when a new screenshot is added.
 */
public class ScreenshotAddedCallback extends CallbackMsg {

    private EResult result;

    private UGCHandle screenshotID;

    public ScreenshotAddedCallback(JobID jobID, CMsgClientUCMAddScreenshotResponse.Builder msg) {
        setJobID(jobID);

        result = EResult.from(msg.getEresult());
        screenshotID = new UGCHandle(msg.getScreenshotid());
    }

    public EResult getResult() {
        return result;
    }

    public UGCHandle getScreenshotID() {
        return screenshotID;
    }
}
