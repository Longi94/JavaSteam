package in.dragonbra.javasteam.steam.handlers.steamcloud.callback;

import in.dragonbra.javasteam.enums.EResult;
import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserver.CMsgClientUFSGetUGCDetailsResponse;
import in.dragonbra.javasteam.steam.handlers.steamcloud.SteamCloud;
import in.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackMsg;
import in.dragonbra.javasteam.types.JobID;
import in.dragonbra.javasteam.types.SteamID;
import in.dragonbra.javasteam.types.UGCHandle;

/**
 * This callback is received in response to calling {@link SteamCloud#requestUGCDetails(UGCHandle)}.
 */
public class UGCDetailsCallback extends CallbackMsg {

    private EResult result;

    private int appID;

    private SteamID creator;

    private String url;

    private String fileName;

    private int fileSize;

    public UGCDetailsCallback(JobID jobID, CMsgClientUFSGetUGCDetailsResponse.Builder msg) {
        setJobID(jobID);

        result = EResult.from(msg.getEresult());

        appID = msg.getAppId();
        creator = new SteamID(msg.getSteamidCreator());

        url = msg.getUrl();

        fileName = msg.getFilename();
        fileSize = msg.getFileSize();
    }

    public EResult getResult() {
        return result;
    }

    public int getAppID() {
        return appID;
    }

    public SteamID getCreator() {
        return creator;
    }

    public String getUrl() {
        return url;
    }

    public String getFileName() {
        return fileName;
    }

    public int getFileSize() {
        return fileSize;
    }
}
