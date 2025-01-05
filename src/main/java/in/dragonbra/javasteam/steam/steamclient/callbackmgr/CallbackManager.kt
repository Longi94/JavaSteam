package `in`.dragonbra.javasteam.steam.steamclient.callbackmgr

import com.google.protobuf.GeneratedMessage
import `in`.dragonbra.javasteam.steam.handlers.steamunifiedmessages.SteamUnifiedMessages
import `in`.dragonbra.javasteam.steam.handlers.steamunifiedmessages.UnifiedService
import `in`.dragonbra.javasteam.steam.handlers.steamunifiedmessages.callback.ServiceMethodNotification
import `in`.dragonbra.javasteam.steam.handlers.steamunifiedmessages.callback.ServiceMethodResponse
import `in`.dragonbra.javasteam.steam.steamclient.SteamClient
import `in`.dragonbra.javasteam.types.JobID
import `in`.dragonbra.javasteam.util.compat.Consumer
import java.io.Closeable
import java.util.*
import java.util.concurrent.*

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

    private val steamUnifiedMessages: SteamUnifiedMessages = steamClient.getHandler(SteamUnifiedMessages::class.java)!!

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
    // TODO: Add Kotlin coroutines version.
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
    @Suppress("unused")
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

    /**
     * Registers a callback to receive service notifications from Steam's unified messaging system.
     *
     * This method creates a service subscription that listens for specific notifications from a Steam service.
     * When a notification arrives, it validates the notification type and forwards it to the provided callback
     * function if the types match.
     *
     * @param TService The type of Steam service to subscribe to (e.g., GameNotificationsClient)
     * @param TNotification The type of notification message to receive (e.g., CGameNotifications_OnNotificationsRequested_Notification.Builder)
     * @param serviceClass The class object representing the Steam service
     * @param notificationClass The class object representing the notification type
     * @param callbackFunc The callback function to be invoked when matching notifications are received
     * @return A [Closeable] subscription. Call [Closeable.close] to unsubscribe and clean up resources
     *
     * Example usage in Kotlin:
     * val subscription = manager.subscribeServiceNotification(
     *     GameNotificationsClient::class.java,
     *     CGameNotifications_OnNotificationsRequested_Notification.Builder::class.java,
     *     this::onGameStartedNotification
     * )
     *
     * Example usage in Java:
     * manager.subscribeServiceNotification(
     *     GameNotificationsClient.class,
     *     CGameNotifications_OnNotificationsRequested_Notification.Builder.class,
     *     notification -> onGameStartedNotification(notification)
     * );
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

        val callback = Callback(
            callbackType = ServiceMethodNotification::class.java as Class<out ServiceMethodNotification<TNotification>>,
            onRun = wrappedCallback,
            mgr = this,
            jobID = JobID.INVALID
        )

        return callback
    }

    /**
     * Registers a callback to receive service responses from Steam's unified messaging system.
     *
     * This method creates a service subscription that listens for specific responses from a Steam service.
     * When a response is received, it validates the response type and forwards it to the provided callback
     * function if the types match. Unlike notifications, responses are typically used for request-response
     * patterns in the Steam API.
     *
     * @param TService The type of Steam service to subscribe to (e.g., GameNotificationsClient)
     * @param TNotification The type of response message to receive
     * @param serviceClass The class object representing the Steam service
     * @param notificationClass The class object representing the response type
     * @param callbackFunc The callback function to be invoked when matching responses are received
     * @return A [Closeable] subscription. Call [Closeable.close] to unsubscribe and clean up resources
     *
     * @see ServiceMethodResponse
     * @see UnifiedService
     *
     * Example usage in Kotlin:
     * val subscription = manager.subscribeServiceResponse(
     *     Player::class.java,
     *     CPlayer_GetGameBadgeLevels_Response.Builder::class.java
     * ) { response ->
     *     println("Badge level: ${response.body.playerLevel}")
     * }
     *
     * Example usage in Java:
     * Closeable subscription = manager.subscribeServiceResponse(
     *     Player.class,
     *     CPlayer_GetGameBadgeLevels_Response.Builder.class,
     *     response -> {
     *         System.out.println("Badge level: " + response.getBody().getPlayerLevel());
     *     }
     * );
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

        val callback = Callback(
            callbackType = ServiceMethodResponse::class.java as Class<out ServiceMethodResponse<TNotification>>,
            onRun = wrappedCallback,
            mgr = this,
            jobID = JobID.INVALID,
        )

        return callback
    }

    //region Kotlin-Helpers
    @JvmSynthetic
    inline fun <reified TCallback : CallbackMsg> subscribe(
        jobID: JobID = JobID.INVALID,
        noinline callbackFunc: (TCallback) -> Unit,
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

        // find handlers interested in this callback
        callbacks.forEach { callback ->
            if (callback.callbackType.isAssignableFrom(type)) {
                callback.run(call)
            }
        }
    }
}
