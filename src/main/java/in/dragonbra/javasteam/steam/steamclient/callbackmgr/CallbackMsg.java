package in.dragonbra.javasteam.steam.steamclient.callbackmgr;

import in.dragonbra.javasteam.types.JobID;

/**
 * @author lngtr
 * @since 2018-02-22
 */
public class CallbackMsg implements ICallbackMsg {

    private JobID jobID = JobID.INVALID;

    @Override
    public JobID getJobID() {
        return this.jobID;
    }

    @Override
    public void setJobID(JobID jobID) {
        this.jobID = jobID;
    }
}
