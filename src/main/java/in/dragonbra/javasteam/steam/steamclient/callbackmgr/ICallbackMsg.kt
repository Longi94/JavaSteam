package `in`.dragonbra.javasteam.steam.steamclient.callbackmgr

import `in`.dragonbra.javasteam.types.JobID

/**
 * A callback message
 */
interface ICallbackMsg {

    /**
     * The [JobID] that this callback is associated with. If there is no job associated,
     * then this will be [JobID.INVALID]
     */
    var jobID: JobID
}
