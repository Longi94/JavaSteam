package in.dragonbra.javasteam.steam.handlers.steamcloud.callback;

import in.dragonbra.javasteam.enums.EResult;
import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserverUfs.CMsgClientUFSGetSingleFileInfoResponse;
import in.dragonbra.javasteam.steam.handlers.steamcloud.SteamCloud;
import in.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackMsg;
import in.dragonbra.javasteam.types.JobID;

import java.util.Date;

/**
 * This callback is received in response to calling {@link SteamCloud#getSingleFileInfo(int, String)}.
 */
public class SingleFileInfoCallback extends CallbackMsg {

    private EResult result;

    private int appID;

    private String fileName;

    private byte[] shaHash;

    private Date timestamp;

    private int fileSize;

    private boolean isExplicitDelete;

    public SingleFileInfoCallback(JobID jobID, CMsgClientUFSGetSingleFileInfoResponse.Builder msg) {
        setJobID(jobID);

        result = EResult.from(msg.getEresult());

        appID = msg.getAppId();
        fileName = msg.getFileName();
        shaHash = msg.getShaFile().toByteArray();
        timestamp = new Date(msg.getTimeStamp() * 1000L);
        fileSize = msg.getRawFileSize();
        isExplicitDelete = msg.getIsExplicitDelete();
    }

    /**
     * @return the result of the request.
     */
    public EResult getResult() {
        return result;
    }

    /**
     * @return the App ID the file is for.
     */
    public int getAppID() {
        return appID;
    }

    /**
     * @return the file name request.
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * @return the SHA hash of the file.
     */
    public byte[] getShaHash() {
        return shaHash;
    }

    /**
     * @return the timestamp of the file.
     */
    public Date getTimestamp() {
        return timestamp;
    }

    /**
     * @return the size of the file.
     */
    public int getFileSize() {
        return fileSize;
    }

    /**
     * @return if the file was explicitly deleted by the user.
     */
    public boolean isExplicitDelete() {
        return isExplicitDelete;
    }
}
