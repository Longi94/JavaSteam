package in.dragonbra.javasteam.steam.steamclient.callbackmgr;

import in.dragonbra.javasteam.types.JobID;

/**
 * A callback message
 */
public interface ICallbackMsg {
    JobID getJobID();

    void setJobID(JobID jobID);
}
