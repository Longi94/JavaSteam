package `in`.dragonbra.javasteam.types

import `in`.dragonbra.javasteam.steam.steamclient.SteamClient
import `in`.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackMsg
import java.time.Instant

/**
 * The base class for awaitable versions of a [JobID].
 * Should not be used or constructed directly, but rather with [AsyncJobSingle] or [AsyncJobMultiple]
 *
 * @author Lossy
 * @since 2023-03-17
 */
abstract class AsyncJob(val client: SteamClient, val jobID: JobID) {

    private var jobStart = Instant.now()

    var timeout: Long = 10000 // 10 Seconds

    val isTimedOut: Boolean
        get() = Instant.now() >= jobStart.plusMillis(timeout)

    protected fun registerJob(client: SteamClient) {
        client.startJob(this)
    }

    abstract fun addResult(callback: CallbackMsg): Boolean

    abstract fun setFailed(dueToRemoteFailure: Boolean)

    fun heartbeat() {
        println("heartbeat")
        timeout += 10000
    }
}
