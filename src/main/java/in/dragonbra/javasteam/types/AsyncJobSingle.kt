package `in`.dragonbra.javasteam.types

import `in`.dragonbra.javasteam.steam.steamclient.AsyncJobFailedException
import `in`.dragonbra.javasteam.steam.steamclient.SteamClient
import `in`.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackMsg
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.future.asCompletableFuture
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutionException

/**
 * @author Lossy
 * @since 2023-03-17
 */
class AsyncJobSingle<T : CallbackMsg>(client: SteamClient, jobId: JobID) : AsyncJob(client, jobId) {

    private val deferred = CompletableDeferred<T>()

    // One view of the deferred, so every toFuture() call hands back the same instance
    private val future: CompletableFuture<T> = deferred.asCompletableFuture()

    init {
        registerJob(client)
    }

    /**
     * Suspends until this job completes. The caller resumes on its own dispatcher,
     * not on the network thread that completed the job.
     */
    suspend fun await(): T = deferred.await()

    /**
     * Java interop. Always returns the same future.
     *
     * Non-async continuations (`thenAccept`, `thenApply`, ...) run on the thread that completes the job,
     * which is normally the network thread. Use the `*Async` variants for anything slow or blocking.
     */
    fun toFuture(): CompletableFuture<T> = future

    @Suppress("unused")
    @Throws(ExecutionException::class, InterruptedException::class)
    fun runBlock(): T = toFuture().get()

    override fun addResult(callback: CallbackMsg): Boolean {
        // we're complete with just this callback
        @Suppress("UNCHECKED_CAST")
        deferred.complete(callback as T)

        // inform steamclient that this job wishes to be removed from tracking since
        // we've received the single callback we were waiting for
        return true
    }

    override fun setFailed(dueToRemoteFailure: Boolean) {
        if (dueToRemoteFailure) {
            // if steam informs us of a remote failure, we complete with exception
            deferred.completeExceptionally(AsyncJobFailedException())
        } else {
            // if we time out, we cancel the future
            deferred.cancel()
        }
    }
}
