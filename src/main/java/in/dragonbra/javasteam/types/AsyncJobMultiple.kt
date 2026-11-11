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

    // One view of the deferred, so every toFuture() call hands back the same instance
    private val future: CompletableFuture<ResultSet<T>> = deferred.asCompletableFuture()

    private val results = mutableListOf<T>()

    init {
        registerJob(client)
    }

    @Deprecated("Use toFuture() instead", ReplaceWith("toFuture()"))
    fun toDeferred(): CompletableFuture<ResultSet<T>> = toFuture()

    /**
     * Java interop. Always returns the same future.
     *
     * Non-async continuations (`thenAccept`, `thenApply`, ...) run on the thread that completes the job,
     * which is normally the network thread. Use the `*Async` variants for anything slow or blocking.
     */
    fun toFuture(): CompletableFuture<ResultSet<T>> = future

    /**
     * Suspends until this job completes. The caller resumes on its own dispatcher,
     * not on the network thread that completed the job.
     */
    suspend fun await(): ResultSet<T> = deferred.await()

    @Suppress("unused")
    @Throws(CancellationException::class)
    fun runBlock(): ResultSet<T> = toFuture().get()

    @Synchronized
    override fun addResult(callback: CallbackMsg): Boolean {
        @Suppress("UNCHECKED_CAST")
        val callbackMsg = callback as T

        results.add(callbackMsg)

        return if (finishCondition(callbackMsg) == true) {
            deferred.complete(
                ResultSet(
                    complete = true,
                    failed = false,
                    results = Collections.unmodifiableList(results.toList())
                )
            )
            true
        } else {
            heartbeat()
            false
        }
    }

    @Synchronized
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
            deferred.complete(ResultSet(false, dueToRemoteFailure, Collections.unmodifiableList(results.toList())))
        }
    }
}
