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

    init {
        registerJob(client)
    }

    // Kotlin
    suspend fun await(): T = deferred.await()

    // Java interop
    fun toFuture(): CompletableFuture<T> = deferred.asCompletableFuture()

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
