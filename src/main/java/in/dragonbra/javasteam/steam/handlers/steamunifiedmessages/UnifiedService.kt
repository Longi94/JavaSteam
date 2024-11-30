package `in`.dragonbra.javasteam.steam.handlers.steamunifiedmessages

import com.google.protobuf.AbstractMessage
import com.google.protobuf.GeneratedMessage
import `in`.dragonbra.javasteam.base.PacketClientMsgProtobuf

/**
 * @author Lossy
 * @since 2024-10-22
 *
 * @constructor Abstract definition of a steam unified messages service.
 * @property unifiedMessages A reference to the [SteamUnifiedMessages] instance this service was created from.
 */
@Suppress("unused")
abstract class UnifiedService(val unifiedMessages: SteamUnifiedMessages? = null) {

    /**
     * Handles a response message for this service. This should not be called directly.
     * @param methodName The name of the method the service should handle.
     * @param packetMsg The packet message that contains the data.
     */
    abstract fun handleResponseMsg(methodName: String, packetMsg: PacketClientMsgProtobuf)

    /**
     * Handles a notification message for this service. This should not be called directly.
     * @param methodName The name of the method the service should handle.
     * @param packetMsg The packet message that contains the data.
     */
    abstract fun handleNotificationMsg(methodName: String, packetMsg: PacketClientMsgProtobuf)

    /**
     * Dispatches the provided data as a service method response.
     * @param TResponse The type of the response.
     * @param serviceClass The proto class of the response
     * @param packetMsg The packet message that contains the data.
     */
    protected fun <TResponse : GeneratedMessage.Builder<TResponse>> postResponseMsg(
        serviceClass: Class<out AbstractMessage>,
        packetMsg: PacketClientMsgProtobuf,
    ) {
        unifiedMessages?.handleResponseMsg(serviceClass, packetMsg)
    }

    /**
     * Dispatches the provided data as a service method notification.
     * @param TNotification The type of the notification.
     * @param serviceClass The proto class of the notification
     * @param packetMsg The packet message that contains the data.
     */
    protected fun <TNotification : GeneratedMessage.Builder<TNotification>> postNotificationMsg(
        serviceClass: Class<out AbstractMessage>,
        packetMsg: PacketClientMsgProtobuf,
    ) {
        unifiedMessages?.handleNotificationMsg(serviceClass, packetMsg)
    }

    /**
     * The name of the steam unified messages service.
     */
    abstract val serviceName: String
}
