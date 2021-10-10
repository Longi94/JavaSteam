package in.dragonbra.javasteam.steam.handlers.steamcloud.callback;

import in.dragonbra.javasteam.enums.EResult;
import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserverUfs.CMsgClientUFSShareFileResponse;
import in.dragonbra.javasteam.steam.handlers.steamcloud.SteamCloud;
import in.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackMsg;
import in.dragonbra.javasteam.types.JobID;

/**
 * This callback is received in response to calling {@link SteamCloud#shareFile(int, String)}.
 */
public class ShareFileCallback extends CallbackMsg {

    private EResult result;

    private long ugcId;

    public ShareFileCallback(JobID jobID, CMsgClientUFSShareFileResponse.Builder msg) {
        setJobID(jobID);

        result = EResult.from(msg.getEresult());
        ugcId = msg.getHcontent();
    }

    /**
     * @return the result of the request.
     */
    public EResult getResult() {
        return result;
    }

    /**
     * @return the resulting UGC handle.
     */
    public long getUgcId() {
        return ugcId;
    }
}
