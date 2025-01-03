package `in`.dragonbra.javasteam.types

import `in`.dragonbra.javasteam.steam.steamclient.AsyncJobFailedException
import `in`.dragonbra.javasteam.steam.steamclient.SteamClient
import `in`.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackMsg
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.future.await
import java.util.concurrent.*

/**
 * @author Lossy
 * @since 2023-03-17
 */
class AsyncJobSingle<T : CallbackMsg>(client: SteamClient, jobId: JobID) : AsyncJob(client, jobId) {

    private val future = CompletableFuture<T>()

    init {
        registerJob(client)
    }

    @Deprecated("Use toFuture() instead", ReplaceWith("toFuture()"))
    fun toDeferred(): CompletableFuture<T> = toFuture()

    fun toFuture(): CompletableFuture<T> = future

    suspend fun await(): T = future.await()

    @Suppress("unused")
    @Throws(CancellationException::class)
    fun runBlock(): T = toFuture().get()

    override fun addResult(callback: CallbackMsg): Boolean {
        // we're complete with just this callback
        @Suppress("UNCHECKED_CAST")
        future.complete(callback as T)

        // inform steamclient that this job wishes to be removed from tracking since
        // we've received the single callback we were waiting for
        return true
    }

    override fun setFailed(dueToRemoteFailure: Boolean) {
        if (dueToRemoteFailure) {
            // if steam informs us of a remote failure, we complete with exception
            future.completeExceptionally(AsyncJobFailedException())
        } else {
            // if we time out, we cancel the future
            future.cancel(true)
        }
    }
}
