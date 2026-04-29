package `in`.dragonbra.javasteam.steam.steamclient.callbackmgr

import com.google.protobuf.GeneratedMessage
import `in`.dragonbra.javasteam.steam.handlers.steamunifiedmessages.SteamUnifiedMessages
import `in`.dragonbra.javasteam.steam.handlers.steamunifiedmessages.UnifiedService
import `in`.dragonbra.javasteam.steam.handlers.steamunifiedmessages.callback.ServiceMethodNotification
import `in`.dragonbra.javasteam.steam.handlers.steamunifiedmessages.callback.ServiceMethodResponse
import `in`.dragonbra.javasteam.steam.steamclient.SteamClient
import `in`.dragonbra.javasteam.types.JobID
import `in`.dragonbra.javasteam.util.compat.Consumer
import kotlinx.coroutines.runBlocking
import java.io.Closeable
import java.util.*
import java.util.concurrent.*

/**
 * A utility for routing callbacks to function calls.
 * In order to bind callbacks to functions, an instance of this class must be created for the
 * [SteamClient] instance that will be posting callbacks.
 *
 * @param steamClient The [SteamClient] instance to handle the callbacks of.
 */
class CallbackManager(private val steamClient: SteamClient) {

    private val registeredCallbacks: MutableSet<CallbackBase> = Collections.newSetFromMap(ConcurrentHashMap())

    private val steamUnifiedMessages: SteamUnifiedMessages = steamClient.getHandler(SteamUnifiedMessages::class.java)!!

    /**
     * Runs a single queued callback. Returns immediately if no callback is queued.
     *
     * @return `true` if a callback was run, `false` otherwise.
     */
    fun runCallbacks(): Boolean {
        val call = steamClient.getCallback() ?: return false
        handle(call)
        return true
    }

    /**
     * Blocks the current thread to run a single queued callback.
     * If no callback is queued, blocks for up to [timeout] milliseconds or until one becomes available.
     *
     * If any asynchronous callbacks are registered, they will be blocked on synchronously.
     * Use [runWaitCallbackAsync] to properly await asynchronous callbacks.
     *
     * @param timeout The length of time to block in milliseconds.
     * @return `true` if a callback was run, `false` if the timeout elapsed.
     */
    fun runWaitCallbacks(timeout: Long): Boolean {
        val call = steamClient.waitForCallback(timeout) ?: return false
        handle(call)
        return true
    }

    /**
     * Blocks the current thread to run all queued callbacks, then returns once the queue is empty.
     * If no callback is queued, blocks for up to [timeout] milliseconds or until one becomes available.
     *
     * If any asynchronous callbacks are registered, they will be blocked on synchronously.
     * Use [runWaitCallbackAsync] to properly await asynchronous callbacks.
     *
     * @param timeout The length of time to block in milliseconds.
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
     * If no callback is queued, blocks indefinitely until one becomes available.
     *
     * If any asynchronous callbacks are registered, they will be blocked on synchronously.
     * Use [runWaitCallbackAsync] to properly await asynchronous callbacks.
     */
    fun runWaitCallbacks() {
        val call = steamClient.waitForCallback()
        handle(call)
    }

    /**
     * Asynchronously awaits a single queued callback.
     * If no callback is queued, suspends until one becomes available.
     */
    @Suppress("unused")
    suspend fun runWaitCallbackAsync() {
        val call = steamClient.waitForCallbackAsync()
        handleAsync(call)
    }

    /**
     * Subscribes to callbacks of type [TCallback] with an optional [JobID] filter.
     *
     * @param TCallback The type of callback to subscribe to.
     * @param callbackType The class of the callback type.
     * @param jobID Only callbacks matching this [JobID] will be received.
     *   Use [JobID.INVALID] to receive all callbacks of this type.
     * @param callbackFunc The function to invoke when a matching callback is received.
     * @return A [Closeable] that unsubscribes [callbackFunc] when closed.
     */
    fun <TCallback : CallbackMsg> subscribe(
        callbackType: Class<out TCallback>,
        jobID: JobID,
        callbackFunc: Consumer<TCallback>,
    ): Closeable = Callback(callbackType, callbackFunc::accept, this, jobID)

    /**
     * Subscribes to all callbacks of type [TCallback].
     *
     * @param TCallback The type of callback to subscribe to.
     * @param callbackType The class of the callback type.
     * @param callbackFunc The function to invoke when a matching callback is received.
     * @return A [Closeable] that unsubscribes [callbackFunc] when closed.
     */
    fun <TCallback : CallbackMsg> subscribe(
        callbackType: Class<out TCallback>,
        callbackFunc: Consumer<TCallback>,
    ): Closeable = subscribe(callbackType, JobID.INVALID, callbackFunc)

    /**
     * Subscribes to callbacks of type [TCallback] with a suspending callback and an optional [JobID] filter.
     *
     * Use [runWaitCallbackAsync] to properly await the suspending [callbackFunc].
     *
     * @param TCallback The type of callback to subscribe to.
     * @param callbackType The class of the callback type.
     * @param jobID Only callbacks matching this [JobID] will be received.
     *   Use [JobID.INVALID] to receive all callbacks of this type.
     * @param callbackFunc The suspending function to invoke when a matching callback is received.
     * @return A [Closeable] that unsubscribes [callbackFunc] when closed.
     */
    fun <TCallback : CallbackMsg> subscribe(
        callbackType: Class<out TCallback>,
        jobID: JobID,
        callbackFunc: suspend (TCallback) -> Unit,
    ): Closeable = Callback(callbackType, callbackFunc, this, jobID)

    /**
     * Subscribes to all callbacks of type [TCallback] with a suspending callback.
     *
     * Use [runWaitCallbackAsync] to properly await the suspending [callbackFunc].
     *
     * @param TCallback The type of callback to subscribe to.
     * @param callbackType The class of the callback type.
     * @param callbackFunc The suspending function to invoke when a matching callback is received.
     * @return A [Closeable] that unsubscribes [callbackFunc] when closed.
     */
    fun <TCallback : CallbackMsg> subscribe(
        callbackType: Class<out TCallback>,
        callbackFunc: suspend (TCallback) -> Unit,
    ): Closeable = subscribe(callbackType, JobID.INVALID, callbackFunc)

    /**
     * Subscribes to service notifications of type [TNotification] from the [TService] Steam unified service.
     *
     * @param TService The unified service type to subscribe to.
     * @param TNotification The notification message type to receive.
     * @param serviceClass The class of the unified service.
     * @param notificationClass The class of the notification type.
     * @param callbackFunc The function to invoke when a matching notification is received.
     * @return A [Closeable] that unsubscribes [callbackFunc] when closed.
     */
    @Suppress("UNCHECKED_CAST")
    fun <TService : UnifiedService, TNotification : GeneratedMessage.Builder<TNotification>> subscribeServiceNotification(
        serviceClass: Class<TService>,
        notificationClass: Class<TNotification>,
        callbackFunc: Consumer<ServiceMethodNotification<TNotification>>,
    ): Closeable {
        steamUnifiedMessages.createService(serviceClass)

        // wrappedCallback checks that the notification body matches the expected type
        // before passing it to callbackFunc, preventing ClassCastException due to type erasure.
        val wrappedCallback = Consumer<ServiceMethodNotification<TNotification>> { notification ->
            if (notification.body::class.java == notificationClass) {
                callbackFunc.accept(notification as ServiceMethodNotification<TNotification>)
            }
        }

        return Callback(
            callbackType = ServiceMethodNotification::class.java as Class<out ServiceMethodNotification<TNotification>>,
            onRun = wrappedCallback::accept,
            mgr = this,
            jobID = JobID.INVALID
        )
    }

    /**
     * Subscribes to service responses of type [TNotification] from the [TService] Steam unified service.
     *
     * @param TService The unified service type to subscribe to.
     * @param TNotification The response message type to receive.
     * @param serviceClass The class of the unified service.
     * @param notificationClass The class of the response type.
     * @param callbackFunc The function to invoke when a matching response is received.
     * @return A [Closeable] that unsubscribes [callbackFunc] when closed.
     */
    @Suppress("UNCHECKED_CAST")
    fun <TService : UnifiedService, TNotification : GeneratedMessage.Builder<TNotification>> subscribeServiceResponse(
        serviceClass: Class<TService>,
        notificationClass: Class<TNotification>,
        callbackFunc: Consumer<ServiceMethodResponse<TNotification>>,
    ): Closeable {
        steamUnifiedMessages.createService(serviceClass)

        // wrappedCallback checks that the notification body matches the expected type
        // before passing it to callbackFunc, preventing ClassCastException due to type erasure.
        val wrappedCallback = Consumer<ServiceMethodResponse<TNotification>> { notification ->
            if (notification.body::class.java == notificationClass) {
                callbackFunc.accept(notification as ServiceMethodResponse<TNotification>)
            }
        }

        return Callback(
            callbackType = ServiceMethodResponse::class.java as Class<out ServiceMethodResponse<TNotification>>,
            onRun = wrappedCallback::accept,
            mgr = this,
            jobID = JobID.INVALID,
        )
    }

    //region Kotlin-Helpers

    // @JvmSynthetic
    // inline fun <reified TCallback : CallbackMsg> subscribe(
    //     jobID: JobID = JobID.INVALID,
    //     noinline callbackFunc: (TCallback) -> Unit,
    // ): Closeable = subscribe(TCallback::class.java, jobID, callbackFunc)

    @JvmSynthetic
    inline fun <reified TCallback : CallbackMsg> subscribe(
        jobID: JobID = JobID.INVALID,
        noinline callbackFunc: suspend (TCallback) -> Unit,
    ): Closeable = subscribe(TCallback::class.java, jobID, callbackFunc)

    @JvmSynthetic
    inline fun <reified TCallback : CallbackMsg> subscribe(
        callbackFunc: Consumer<TCallback>,
    ): Closeable = subscribe(TCallback::class.java, JobID.INVALID, callbackFunc)

    @JvmSynthetic
    @Suppress("unused")
    inline fun <reified TService : UnifiedService, reified TNotification : GeneratedMessage.Builder<TNotification>> subscribeServiceNotification(
        noinline callback: (ServiceMethodNotification<TNotification>) -> Unit,
    ): Closeable = subscribeServiceNotification(TService::class.java, TNotification::class.java, callback)

    @JvmSynthetic
    @Suppress("unused")
    inline fun <reified TService : UnifiedService, reified TNotification : GeneratedMessage.Builder<TNotification>> subscribeServiceResponse(
        noinline callback: (ServiceMethodResponse<TNotification>) -> Unit,
    ): Closeable = subscribeServiceResponse(TService::class.java, TNotification::class.java, callback)
    //endregion

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
        callbacks.forEach { callback ->
            if (callback.callbackType.isAssignableFrom(type)) {
                runBlocking { callback.run(call) }
            }
        }
    }

    private suspend fun handleAsync(call: CallbackMsg) {
        val callbacks = registeredCallbacks
        val type = call.javaClass
        callbacks.forEach { callback ->
            if (callback.callbackType.isAssignableFrom(type)) {
                callback.run(call)
            }
        }
    }
}
