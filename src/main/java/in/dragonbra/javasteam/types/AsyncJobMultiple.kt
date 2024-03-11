package `in`.dragonbra.javasteam.types

import `in`.dragonbra.javasteam.steam.steamclient.AsyncJobFailedException
import `in`.dragonbra.javasteam.steam.steamclient.SteamClient
import `in`.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackMsg
import kotlinx.coroutines.CompletableDeferred
import java.util.*

/**
 * @author Lossy
 * @since 2023-03-17
 */
class AsyncJobMultiple<T : CallbackMsg>(
    client: SteamClient,
    jobId: JobID,
    private val finishCondition: (T) -> Boolean?
) : AsyncJob(client, jobId) {

    class ResultSet(
        var complete: Boolean = false,
        var failed: Boolean = false,
        var results: List<CallbackMsg> = listOf()
    )

    private val tcs = CompletableDeferred<ResultSet>()

    private val results = mutableListOf<T>()

    init {
        registerJob(client)
    }

    fun toDeferred(): CompletableDeferred<ResultSet> {
        return tcs
    }

    override fun addResult(callback: CallbackMsg?): Boolean {
        if (callback == null) {
            throw IllegalArgumentException("callback is null")
        }

        @Suppress("UNCHECKED_CAST")
        val callbackMsg = callback as T

        // add this callback to our result set
        results.add(callbackMsg)

        return if (finishCondition(callbackMsg) == true) {
            tcs.complete(ResultSet(complete = true, failed = false, results = Collections.unmodifiableList(results)))
            true
        } else {
            heartbeat()
            false
        }
    }

    override fun setFailed(dueToRemoteFailure: Boolean) {
        if (results.size == 0) {
            // if we have zero callbacks in our result set, we cancel this task
            if (dueToRemoteFailure) {
                // if we're canceling with a remote failure, post a job failure exception
                tcs.completeExceptionally(AsyncJobFailedException())
            } else {
                // otherwise, normal task cancellation for timeouts
                tcs.cancel()
            }
        } else {
            val resultSet = ResultSet(false, dueToRemoteFailure, Collections.unmodifiableList(results))

            tcs.complete(resultSet)
        }
    }
}
