package in.dragonbra.javasteam.steam.handlers.steamapps.callback;

import in.dragonbra.javasteam.enums.EResult;
import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserver2.CMsgClientRedeemGuestPassResponse;
import in.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackMsg;
import in.dragonbra.javasteam.types.JobID;

public class RedeemGuestPassResponseCallback extends CallbackMsg {

    private EResult result;

    private Integer packageID;

    private Integer mustOwnAppID;

    public RedeemGuestPassResponseCallback(JobID jobID, CMsgClientRedeemGuestPassResponse.Builder msg) {
        setJobID(jobID);

        this.result = EResult.from(msg.getEresult());
        this.packageID = msg.getPackageId();
        this.mustOwnAppID = msg.getMustOwnAppid();
    }

    /**
     * @return Result of the operation
     */
    public EResult getResult() {
        return result;
    }

    /**
     * @return Result of the operation
     */
    public Integer getPackageID() {
        return packageID;
    }

    /**
     * @return App ID which must be owned to activate this guest pass.
     */
    public Integer getMustOwnAppID() {
        return mustOwnAppID;
    }
}
