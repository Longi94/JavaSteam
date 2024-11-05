package `in`.dragonbra.javasteam.steam.handlers.steamunifiedmessages

import `in`.dragonbra.javasteam.base.PacketClientMsgProtobuf

/**
 * @author Lossy
 * @since 2024-10-22
 *
 * Abstract definition of a steam unified messages service.
 * @constructor unifiedMessages A reference to the [SteamUnifiedMessages] instance this service was created from.
 */
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
     * The name of the steam unified messages service.
     */
    abstract val serviceName: String
}
