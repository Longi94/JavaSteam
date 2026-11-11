package `in`.dragonbra.javasteam.types

import `in`.dragonbra.javasteam.steam.steamclient.SteamClient
import `in`.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackMsg
import java.util.concurrent.atomic.AtomicLong
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.TimeSource

/**
 * The base class for awaitable versions of a [JobID].
 * Should not be used or constructed directly, but rather with [AsyncJobSingle] or [AsyncJobMultiple]
 *
 * @author Lossy
 * @since 2023-03-17
 */
abstract class AsyncJob(val client: SteamClient, val jobID: JobID) {

    // Monotonic, so a wall clock adjustment can neither expire a job early nor keep it alive
    private val jobStart = TimeSource.Monotonic.markNow()

    // Heartbeats arrive on the network thread while callers may set the timeout from any other
    private val timeoutMillis = AtomicLong(DEFAULT_TIMEOUT_MS)

    /**
     * The period of time, in milliseconds, before this job is considered timed out and is canceled.
     * By default this is 10 seconds.
     */
    var timeout: Long
        get() = timeoutMillis.get()
        set(value) = timeoutMillis.set(value)

    val isTimedOut: Boolean
        get() = jobStart.elapsedNow() >= timeout.milliseconds

    protected fun registerJob(client: SteamClient) {
        client.startJob(this)
    }

    abstract fun addResult(callback: CallbackMsg): Boolean

    abstract fun setFailed(dueToRemoteFailure: Boolean)

    fun heartbeat() {
        timeoutMillis.addAndGet(DEFAULT_TIMEOUT_MS)
    }

    private companion object {
        const val DEFAULT_TIMEOUT_MS = 10_000L
    }
}
