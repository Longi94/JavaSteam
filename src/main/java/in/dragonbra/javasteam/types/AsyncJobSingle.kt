package `in`.dragonbra.javasteam.types

import `in`.dragonbra.javasteam.steam.steamclient.AsyncJobFailedException
import `in`.dragonbra.javasteam.steam.steamclient.SteamClient
import `in`.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackMsg
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.runBlocking
import kotlin.jvm.Throws

/**
 * @author Lossy
 * @since 2023-03-17
 */
class AsyncJobSingle<T : CallbackMsg>(client: SteamClient, jobId: JobID) : AsyncJob(client, jobId) {

    private val tcs = CompletableDeferred<T>()

    init {
        registerJob(client)
    }

    fun toDeferred(): CompletableDeferred<T> = tcs

    suspend fun await(): T = toDeferred().await()

    @Throws(CancellationException::class)
    fun runBlock(): T = runBlocking { toDeferred().await() }

    override fun addResult(callback: CallbackMsg): Boolean {
        // we're complete with just this callback
        @Suppress("UNCHECKED_CAST")
        tcs.complete(callback as T)

        // inform steamclient that this job wishes to be removed from tracking since
        // we've received the single callback we were waiting for
        return true
    }

    override fun setFailed(dueToRemoteFailure: Boolean) {
        if (dueToRemoteFailure) {
            // if steam informs us of a remote failure, we cancel with our exception
            tcs.completeExceptionally(AsyncJobFailedException())
        } else {
            // if we time out, we trigger a normal cancellation
            tcs.cancel()
        }
    }
}
