package `in`.dragonbra.javasteam.types

import `in`.dragonbra.javasteam.steam.steamclient.AsyncJobFailedException
import `in`.dragonbra.javasteam.steam.steamclient.SteamClient
import `in`.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackMsg
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.future.asCompletableFuture
import java.util.*
import java.util.concurrent.CompletableFuture

/**
 * @author Lossy
 * @since 2023-03-17
 */
class AsyncJobMultiple<T : CallbackMsg>(
    client: SteamClient,
    jobId: JobID,
    private val finishCondition: (T) -> Boolean?,
) : AsyncJob(client, jobId) {

    class ResultSet<T : CallbackMsg>(
        var complete: Boolean = false,
        var failed: Boolean = false,
        var results: List<T> = listOf(),
    )

    private val deferred = CompletableDeferred<ResultSet<T>>()

    private val results = Collections.synchronizedList(mutableListOf<T>())

    init {
        registerJob(client)
    }

    // Kotlin
    suspend fun await(): ResultSet<T> = deferred.await()

    // Java interop
    fun toFuture(): CompletableFuture<ResultSet<T>> = deferred.asCompletableFuture()

    @Suppress("unused")
    @Throws(CancellationException::class)
    fun runBlock(): ResultSet<T> = toFuture().get()

    override fun addResult(callback: CallbackMsg): Boolean {
        @Suppress("UNCHECKED_CAST")
        val callbackMsg = callback as T

        // add this callback to our result set
        results.add(callbackMsg)

        return if (finishCondition(callbackMsg) == true) {
            val result = ResultSet(
                complete = true,
                failed = false,
                results = Collections.unmodifiableList(results)
            )
            deferred.complete(result)
            true
        } else {
            heartbeat()
            false
        }
    }

    override fun setFailed(dueToRemoteFailure: Boolean) {
        if (results.isEmpty()) {
            // if we have zero callbacks in our result set, we cancel this task
            if (dueToRemoteFailure) {
                // if we're canceling with a remote failure, post a job failure exception
                deferred.completeExceptionally(AsyncJobFailedException())
            } else {
                // otherwise, normal task cancellation for timeouts
                deferred.cancel()
            }
        } else {
            val result = ResultSet(
                complete = false,
                failed = dueToRemoteFailure,
                results = Collections.unmodifiableList(results)
            )
            deferred.complete(result)
        }
    }
}
