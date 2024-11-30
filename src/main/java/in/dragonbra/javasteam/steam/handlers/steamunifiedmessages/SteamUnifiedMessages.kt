package `in`.dragonbra.javasteam.steam.handlers.steamunifiedmessages

import com.google.protobuf.AbstractMessage
import com.google.protobuf.GeneratedMessage
import `in`.dragonbra.javasteam.base.ClientMsgProtobuf
import `in`.dragonbra.javasteam.base.IPacketMsg
import `in`.dragonbra.javasteam.base.PacketClientMsgProtobuf
import `in`.dragonbra.javasteam.enums.EMsg
import `in`.dragonbra.javasteam.steam.handlers.ClientMsgHandler
import `in`.dragonbra.javasteam.steam.handlers.steamunifiedmessages.callback.ServiceMethodNotification
import `in`.dragonbra.javasteam.steam.handlers.steamunifiedmessages.callback.ServiceMethodResponse
import `in`.dragonbra.javasteam.types.AsyncJobSingle
import java.util.concurrent.ConcurrentHashMap

/**
 * @author Lossy
 * @since 2024-10-22
 *
 * This handler is used for interacting with Steamworks unified messaging.
 */
class SteamUnifiedMessages : ClientMsgHandler() {

    internal val handlers = ConcurrentHashMap<String, UnifiedService>()

    /**
     * Creates a service that can be used to send messages and receive notifications via Steamworks unified messaging.
     * @param TService The type of the service to create.
     * @return The instance to create requests from.
     */
    // Kotlin Compat
    @JvmSynthetic
    inline fun <reified TService : UnifiedService> createService(): TService = createService(TService::class.java)

    /**
     * Creates a service that can be used to send messages and receive notifications via Steamworks unified messaging.
     * @param TService The type of the service to create.
     * @param serviceClass The type of the service to create.
     * @return The instance to create requests.
     */
    // Java Compat
    @Suppress("UNCHECKED_CAST")
    fun <TService : UnifiedService> createService(serviceClass: Class<TService>): TService =
        serviceClass.getDeclaredConstructor(SteamUnifiedMessages::class.java)
            .newInstance(this@SteamUnifiedMessages)
            .let { service ->
                handlers.getOrPut(service.serviceName) { service } as TService
            }

    /**
     * Removes a service so it no longer can be used to send messages or receive notifications.
     * @param TService The type of the service to remove.
     */
    // Kotlin Compat
    @Suppress("unused")
    @JvmSynthetic
    inline fun <reified TService : UnifiedService> removeService() {
        removeService(TService::class.java)
    }

    /**
     * Removes a service so it no longer can be used to send messages or receive notifications.
     * @param TService The type of the service to remove.
     * @param serviceClass The type of the service to remove.
     */
    fun <TService : UnifiedService> removeService(serviceClass: Class<TService>) {
        val serviceName = serviceClass.getDeclaredConstructor(SteamUnifiedMessages::class.java)
            .newInstance(null)
            .serviceName
        handlers.remove(serviceName)
    }

    /**
     * Sends a message.
     * Results are returned in a [ServiceMethodResponse] with type [TResult].
     * The returned [AsyncJobSingle] can also be awaited to retrieve the callback result.
     * @param TRequest The type of protobuf object.
     * @param TResult The type of the result of the request.
     * @param name Name of the RPC endpoint. Takes the format ServiceName.RpcName.
     * @param message The message to send.
     * @return The JobID of the request. This can be used to find the appropriate [ServiceMethodResponse].
     */
    @Suppress("UNUSED_PARAMETER")
    fun <TRequest : GeneratedMessage.Builder<TRequest>, TResult : GeneratedMessage.Builder<TResult>> sendMessage(
        responseClass: Class<out TResult>, // Type Casting
        name: String,
        message: GeneratedMessage,
    ): AsyncJobSingle<ServiceMethodResponse<TResult>> {
        val eMsg = if (client.steamID == null) {
            EMsg.ServiceMethodCallFromClientNonAuthed
        } else {
            EMsg.ServiceMethodCallFromClient
        }

        val msg = ClientMsgProtobuf<TRequest>(message::class.java, eMsg).apply {
            sourceJobID = client.getNextJobID()
            header.proto.targetJobName = name
            body.mergeFrom(message)
        }

        client.send(msg)

        return AsyncJobSingle(client, msg.sourceJobID)
    }

    /**
     * Sends a notification.
     * @param TRequest The type of protobuf object.
     * @param name Name of the RPC endpoint. Takes the format ServiceName.RpcName.
     * @param message The message to send.
     */
    fun <TRequest : GeneratedMessage.Builder<TRequest>> sendNotification(
        name: String,
        message: GeneratedMessage,
    ) {
        // Notifications do not set source jobid, otherwise Steam server will actively reject this message
        // if the method being used is a "Notification"
        val eMsg = if (client.steamID == null) {
            EMsg.ServiceMethodCallFromClientNonAuthed
        } else {
            EMsg.ServiceMethodCallFromClient
        }
        val msg = ClientMsgProtobuf<TRequest>(message::class.java, eMsg).apply {
            header.proto.targetJobName = name
            body.mergeFrom(message)
        }

        client.send(msg)
    }

    /**
     * Handles a client message. This should not be called directly.
     * @param packetMsg The packet message that contains the data.
     */
    override fun handleMsg(packetMsg: IPacketMsg) {
        val packetMsgProto = packetMsg as? PacketClientMsgProtobuf ?: return

        if (packetMsg.msgType != EMsg.ServiceMethod && packetMsg.msgType != EMsg.ServiceMethodResponse) {
            return
        }

        val jobName = packetMsgProto.header.proto.targetJobName
        if (jobName.isEmpty()) return

        // format: Service.Method#Version
        val dot = jobName.indexOf('.')
        val hash = jobName.lastIndexOf('#')
        if (dot < 0 || hash < 0) return

        val serviceName = jobName.substring(0, dot)
        val handler = handlers[serviceName] ?: return
        val methodName = jobName.substring(dot + 1, hash)

        when (packetMsgProto.msgType) {
            EMsg.ServiceMethodResponse -> handler.handleResponseMsg(methodName, packetMsgProto)
            EMsg.ServiceMethod -> handler.handleNotificationMsg(methodName, packetMsgProto)
            else -> Unit // Ignore everything else.
        }
    }

    internal fun <TResponse : GeneratedMessage.Builder<TResponse>> handleResponseMsg(
        serviceClass: Class<out AbstractMessage>,
        packetMsg: PacketClientMsgProtobuf,
    ) {
        val callback = ServiceMethodResponse<TResponse>(serviceClass, packetMsg)
        client.postCallback(callback)
    }

    internal fun <TNotification : GeneratedMessage.Builder<TNotification>> handleNotificationMsg(
        serviceClass: Class<out AbstractMessage>,
        packetMsg: PacketClientMsgProtobuf,
    ) {
        val callback = ServiceMethodNotification<TNotification>(serviceClass, packetMsg)
        client.postCallback(callback)
    }
}
