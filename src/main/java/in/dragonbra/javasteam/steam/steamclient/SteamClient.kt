package `in`.dragonbra.javasteam.steam.steamclient

import `in`.dragonbra.javasteam.base.IPacketMsg
import `in`.dragonbra.javasteam.enums.EMsg
import `in`.dragonbra.javasteam.steam.CMClient
import `in`.dragonbra.javasteam.steam.authentication.SteamAuthentication
import `in`.dragonbra.javasteam.steam.handlers.ClientMsgHandler
import `in`.dragonbra.javasteam.steam.handlers.steamapps.SteamApps
import `in`.dragonbra.javasteam.steam.handlers.steamauthticket.SteamAuthTicket
import `in`.dragonbra.javasteam.steam.handlers.steamcloud.SteamCloud
import `in`.dragonbra.javasteam.steam.handlers.steamcontent.SteamContent
import `in`.dragonbra.javasteam.steam.handlers.steamfriends.SteamFriends
import `in`.dragonbra.javasteam.steam.handlers.steamgamecoordinator.SteamGameCoordinator
import `in`.dragonbra.javasteam.steam.handlers.steamgameserver.SteamGameServer
import `in`.dragonbra.javasteam.steam.handlers.steammasterserver.SteamMasterServer
import `in`.dragonbra.javasteam.steam.handlers.steammatchmaking.SteamMatchmaking
import `in`.dragonbra.javasteam.steam.handlers.steamnetworking.SteamNetworking
import `in`.dragonbra.javasteam.steam.handlers.steamnotifications.SteamNotifications
import `in`.dragonbra.javasteam.steam.handlers.steamscreenshots.SteamScreenshots
import `in`.dragonbra.javasteam.steam.handlers.steamunifiedmessages.SteamUnifiedMessages
import `in`.dragonbra.javasteam.steam.handlers.steamuser.SteamUser
import `in`.dragonbra.javasteam.steam.handlers.steamuserstats.SteamUserStats
import `in`.dragonbra.javasteam.steam.handlers.steamworkshop.SteamWorkshop
import `in`.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackMsg
import `in`.dragonbra.javasteam.steam.steamclient.callbacks.ConnectedCallback
import `in`.dragonbra.javasteam.steam.steamclient.callbacks.DisconnectedCallback
import `in`.dragonbra.javasteam.steam.steamclient.configuration.SteamConfiguration
import `in`.dragonbra.javasteam.types.AsyncJob
import `in`.dragonbra.javasteam.types.JobID
import `in`.dragonbra.javasteam.util.log.LogManager
import `in`.dragonbra.javasteam.util.log.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull
import java.util.*
import java.util.concurrent.atomic.AtomicLong

/**
 * Represents a single client that connects to the Steam3 network.
 * This class is also responsible for handling the registration of client message handlers and callbacks.
 *
 * @constructor Initializes a new instance of the [SteamClient] class with a specific configuration.
 * @param configuration The configuration to use for this client.
 */
@Suppress("unused")
class SteamClient @JvmOverloads constructor(
    configuration: SteamConfiguration? = SteamConfiguration.createDefault(),
    internal val defaultScope: CoroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob()),
) : CMClient(configuration) {

    private val handlers = HashMap<Class<out ClientMsgHandler>, ClientMsgHandler>(HANDLERS_COUNT)

    private val currentJobId = AtomicLong(0L)

    private val processStartTime: Date

    private val callbackQueue = Channel<CallbackMsg>(Channel.UNLIMITED)

    internal val jobManager: AsyncJobManager // What does this even do now?

    /**
     * Handler used for authenticating on Steam.
     */
    val authentication: SteamAuthentication by lazy { SteamAuthentication(this) }

    init {
        // add this library's handlers
        // notice: SteamFriends should be added before SteamUser due to AccountInfoCallback
        addHandlerCore(SteamFriends())
        addHandlerCore(SteamUser())
        addHandlerCore(SteamApps())
        addHandlerCore(SteamGameCoordinator())
        addHandlerCore(SteamGameServer())
        addHandlerCore(SteamUserStats())
        addHandlerCore(SteamMasterServer())
        addHandlerCore(SteamCloud())
        addHandlerCore(SteamWorkshop())
        addHandlerCore(SteamUnifiedMessages())
        addHandlerCore(SteamScreenshots())
        addHandlerCore(SteamMatchmaking())
        addHandlerCore(SteamNetworking())
        addHandlerCore(SteamContent())
        addHandlerCore(SteamAuthTicket())
        addHandlerCore(SteamNotifications()) // JavaSteam Addition

        if (handlers.size != HANDLERS_COUNT) {
            logger.error("Handlers size didnt match handlers count (${handlers.size}) when initializing")
        }

        processStartTime = Date()

        jobManager = AsyncJobManager()
    }

    //region Handlers
    /**
     * Adds a new handler to the internal list of message handlers.
     * @param handler The handler to add.
     */
    fun addHandler(handler: ClientMsgHandler) {
        require(!handlers.containsKey(handler.javaClass)) {
            "A handler of type ${handler.javaClass} is already registered."
        }

        addHandlerCore(handler)
    }

    private fun addHandlerCore(handler: ClientMsgHandler) {
        handler.setup(this)
        handlers[handler.javaClass] = handler
    }

    /**
     * Removes a registered handler by name.
     * @param handler The handler name to remove.
     */
    fun removeHandler(handler: Class<out ClientMsgHandler>) {
        handlers.remove(handler)
    }

    /**
     * Removes a registered handler.
     * @param handler The handler name to remove.
     */
    fun removeHandler(handler: ClientMsgHandler) {
        removeHandler(handler.javaClass)
    }

    /**
     * Returns a registered handler.
     *
     * @param type The type of the handler to cast to. Must derive from ClientMsgHandler.
     * @param T  The type of the handler to cast to. Must derive from ClientMsgHandler.
     * @return A registered handler on success, or null if the handler could not be found.
     */
    @Suppress("UNCHECKED_CAST")
    fun <T : ClientMsgHandler> getHandler(type: Class<T>): T? = handlers[type] as T?

    /**
     * Kotlin Helper:
     * Returns a registered handler.
     *
     * @param T  The type of the handler to cast to. Must derive from ClientMsgHandler.
     * @return A registered handler on success, or null if the handler could not be found.
     */
    inline fun <reified T : ClientMsgHandler> getHandler(): T? = getHandler(T::class.java)
    //endregion

    //region Callbacks
    /**
     * Gets the next callback object in the queue, and removes it.
     * @return The next callback in the queue, or null if no callback is waiting.
     */
    fun getCallback(): CallbackMsg? = callbackQueue.tryReceive().getOrNull()

    /**
     * Blocks the calling thread until a callback object is posted to the queue, and removes it.
     * @return The callback object from the queue.
     */
    fun waitForCallback(): CallbackMsg = runBlocking(Dispatchers.Default) {
        callbackQueue.receive()
    }

    /**
     * Asynchronously awaits until a callback object is posted to the queue, and removes it.
     * @return The callback object from the queue.
     */
    suspend fun waitForCallbackAsync(): CallbackMsg = callbackQueue.receive()

    /**
     * Blocks the calling thread until a callback object is posted to the queue, or null after the timeout has elapsed.
     * @param timeout The length of time to block in ms.
     * @return A callback object from the queue if a callback has been posted, or null if the timeout has elapsed.
     */
    fun waitForCallback(timeout: Long): CallbackMsg? = runBlocking {
        withTimeoutOrNull(timeout) {
            callbackQueue.receive()
        }
    }

    /**
     * Posts a callback to the queue. This is normally used directly by client message handlers.
     * @param msg The message.
     */
    fun postCallback(msg: CallbackMsg?) {
        if (msg == null) {
            return
        }

        callbackQueue.trySend(msg)
        jobManager.tryCompleteJob(msg.jobID, msg)
    }
//endregion

//region Jobs
    /**
     * Returns the next available JobID for job based messages.
     * @return The next available JobID.
     */
    fun getNextJobID(): JobID = JobID().apply {
        boxID = 0L
        processID = 0L
        sequentialCount = currentJobId.incrementAndGet()
        startTime = processStartTime
    }

    fun startJob(job: AsyncJob) {
        if (!isConnected) {
            job.setFailed(dueToRemoteFailure = true)
            return
        }

        jobManager.startJob(job)
    }
//endregion

    /**
     * Called when a client message is received from the network.
     * @param packetMsg The packet message.
     */
    override fun onClientMsgReceived(packetMsg: IPacketMsg): Boolean {
        // let the underlying CMClient handle this message first
        if (!super.onClientMsgReceived(packetMsg)) {
            return false
        }

        // we want to handle some of the clientMsg's before we pass them along to registered handlers
        when (packetMsg.getMsgType()) {
            EMsg.JobHeartbeat -> handleJobHeartbeat(packetMsg)
            EMsg.DestJobFailed -> handleJobFailed(packetMsg)
            else -> Unit
        }

        // pass along the clientMsg to all registered handlers
        handlers.forEach { (key, value) ->
            try {
                value.handleMsg(packetMsg)
            } catch (e: Exception) {
                logger.debug("Unhandled exception from ${key.name} handlers", e)
                disconnect()
                return false
            }
        }

        return true
    }

    /**
     * Called when the client is securely connected to Steam3.
     */
    override fun onClientConnected() {
        super.onClientConnected()

        jobManager.setTimeoutsEnabled(true)

        ConnectedCallback().also(::postCallback)
    }

    /**
     * Called when the client is physically disconnected from Steam3.
     */
    override fun onClientDisconnected(userInitiated: Boolean) {
        super.onClientDisconnected(userInitiated)

        postCallback(DisconnectedCallback(userInitiated))

        // if we are disconnected, cancel all pending jobs
        jobManager.cancelPendingJobs()

        jobManager.setTimeoutsEnabled(false)

        clearHandlerCaches()
    }

    fun clearHandlerCaches() {
        getHandler<SteamMatchmaking>()?.clearLobbyCache()
    }

    private fun handleJobHeartbeat(packetMsg: IPacketMsg) {
        JobID(packetMsg.getTargetJobID()).let(jobManager::heartbeatJob)
    }

    private fun handleJobFailed(packetMsg: IPacketMsg) {
        JobID(packetMsg.getTargetJobID()).let(jobManager::failJob)
    }

    companion object {
        private val logger: Logger = LogManager.getLogger(SteamClient::class.java)

        private const val HANDLERS_COUNT = 16
    }
}
