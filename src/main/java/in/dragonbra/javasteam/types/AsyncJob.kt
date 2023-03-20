package `in`.dragonbra.javasteam.types

import `in`.dragonbra.javasteam.steam.steamclient.SteamClient
import `in`.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackMsg
import java.time.Instant

/**
 * The base class for awaitable versions of a <see cref="JobID"></see>.
 * Should not be used or constructed directly, but rather with <see cref="AsyncJob{T}"></see>.
 *
 * @author Lossy
 * @since 2023-03-17
 */
abstract class AsyncJob(val client: SteamClient?, val jobID: JobID?) {

    private var jobStart = Instant.now()

    var timeout: Long = 10000 // 10 Seconds

    val isTimedOut: Boolean
        get() = Instant.now() >= jobStart.plusMillis(timeout)

    init {
        requireNotNull(client) { "client must not be null" }
        requireNotNull(jobID) { "jobId must not be null" }
    }

    protected fun registerJob(client: SteamClient) {
        client.startJob(this)
    }

    abstract fun addResult(callback: CallbackMsg?): Boolean

    abstract fun setFailed(dueToRemoteFailure: Boolean)

    fun heartbeat() {
        println("heartbeat")
        timeout += 10000
    }
}