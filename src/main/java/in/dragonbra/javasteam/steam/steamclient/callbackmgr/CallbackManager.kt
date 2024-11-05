package `in`.dragonbra.javasteam.steam.steamclient.callbackmgr

import `in`.dragonbra.javasteam.steam.steamclient.SteamClient
import `in`.dragonbra.javasteam.types.JobID
import `in`.dragonbra.javasteam.util.compat.Consumer
import java.io.Closeable
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * This class is a utility for routing callbacks to function calls.
 * In order to bind callbacks to functions, an instance of this class must be created for the
 * [SteamClient] instance that will be posting callbacks.
 *
 * @constructor Initializes a new instance of the [CallbackManager] class.
 *  @param steamClient The [SteamClient] instance to handle the callbacks of.
 */
class CallbackManager(private val steamClient: SteamClient) {

    private val registeredCallbacks: MutableSet<CallbackBase> = Collections.newSetFromMap(ConcurrentHashMap())

    /**
     * Runs a single queued callback.
     * If no callback is queued, this method will instantly return.
     *
     * @return true if a callback has been run, false otherwise.
     */
    fun runCallbacks(): Boolean {
        val call = steamClient.getCallback() ?: return false

        handle(call)
        return true
    }

    /**
     * Blocks the current thread to run a single queued callback.
     * If no callback is queued, the method will block for the given timeout or until a callback becomes available.
     *
     * @param timeout The length of time to block.
     * @return true if a callback has been run, false otherwise.
     */
    fun runWaitCallbacks(timeout: Long): Boolean {
        val call = steamClient.waitForCallback(timeout) ?: return false

        handle(call)
        return true
    }

    /**
     * Blocks the current thread to run all queued callbacks.
     * If no callback is queued, the method will block for the given timeout or until a callback becomes available.
     * This method returns once the queue has been emptied.
     *
     * @param timeout The length of time to block.
     */
    fun runWaitAllCallbacks(timeout: Long) {
        if (!runWaitCallbacks(timeout)) {
            return
        }

        while (runCallbacks()) {
            //
        }
    }

    /**
     * Blocks the current thread to run a single queued callback.
     * If no callback is queued, the method will block until one becomes available.
     */
    fun runWaitCallbacks() {
        val call = steamClient.waitForCallback()
        handle(call)
    }

    /**
     *
     */
    suspend fun runWaitCallbackAsync() {
        val call = steamClient.waitForCallbackAsync()
        handle(call)
    }

    /**
     * Registers the provided [Consumer] to receive callbacks of type [TCallback]
     *
     * @param TCallback  The type of callback to subscribe to.
     *  If this is [JobID.INVALID],  all callbacks of type [TCallback]  will be received.
     * @param callbackType The type of the callback
     * @param jobID The [JobID]  of the callbacks that should be subscribed to.
     * @param callbackFunc The function to invoke with the callback.
     * @return An [Closeable]. Disposing of the return value will unsubscribe the callbackFunc .
     */
    fun <TCallback : CallbackMsg> subscribe(
        callbackType: Class<out TCallback>,
        jobID: JobID,
        callbackFunc: Consumer<TCallback>,
    ): Closeable {
        val callback = Callback(callbackType, callbackFunc, this, jobID)
        return callback
    }

    /**
     * Registers the provided [Consumer] to receive callbacks of type [TCallback]
     *
     * @param TCallback  The type of callback to subscribe to.
     * @param callbackType type of the callback
     * @param callbackFunc The function to invoke with the callback.
     * @return An [Closeable]. Disposing of the return value will unsubscribe the callbackFunc.
     */
    fun <TCallback : CallbackMsg> subscribe(
        callbackType: Class<out TCallback>,
        callbackFunc: Consumer<TCallback>,
    ): Closeable = subscribe(callbackType, JobID.INVALID, callbackFunc)

    internal fun register(callback: CallbackBase) {
        if (registeredCallbacks.contains(callback)) {
            return
        }

        registeredCallbacks.add(callback)
    }

    internal fun unregister(callback: CallbackBase) {
        registeredCallbacks.remove(callback)
    }

    private fun handle(call: CallbackMsg) {
        val callbacks = registeredCallbacks
        val type = call.javaClass

        // find handlers interested in this callback
        callbacks.forEach { callback ->
            if (callback.callbackType.isAssignableFrom(type)) {
                callback.run(call)
            }
        }
    }
}
