package `in`.dragonbra.javasteam.types

import `in`.dragonbra.javasteam.steam.steamclient.AsyncJobFailedException
import `in`.dragonbra.javasteam.steam.steamclient.SteamClient
import `in`.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackMsg
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.runBlocking

/**
 * @author Lossy
 * @since 2023-03-17
 */
class AsyncJobSingle<T : CallbackMsg>(client: SteamClient, jobId: JobID) : AsyncJob(client, jobId) {

    private val tcs = CompletableDeferred<T>()

    init {
        registerJob(client)
    }

    fun toDeferred(): CompletableDeferred<T> {
        return tcs
    }

    fun runBlock(): T {
        return runBlocking { toDeferred().await() }
    }

    override fun addResult(callback: CallbackMsg?): Boolean {
        requireNotNull(callback) { "callback must not be null" }

        // we're complete with just this callback
        @Suppress("UNCHECKED_CAST")
        tcs.complete(callback as T)

        // inform steamclient that this job wishes to be removed from tracking since
        // we've recieved the single callback we were waiting for
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