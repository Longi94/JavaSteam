package `in`.dragonbra.javasteam.steam.handlers.steamunifiedmessages

import com.google.protobuf.GeneratedMessage
import `in`.dragonbra.javasteam.steam.handlers.steamunifiedmessages.callback.ServiceMethodResponse
import `in`.dragonbra.javasteam.types.AsyncJobSingle

/**
 * @author Lossy
 * @since 2023-01-04
 */
@Suppress("unused")
abstract class UnifiedService(private val steamUnifiedMessages: SteamUnifiedMessages) {

    val className: String
        get() = this.javaClass.simpleName

    /**
     * Sends a message.
     *
     * Results are returned in a [ServiceMethodResponse].
     *
     * @param message    The message to send.
     * @param methodName The Target Job Name.
     * @return The JobID of the message. This can be used to find the appropriate [ServiceMethodResponse].
     */
    fun sendMessage(message: GeneratedMessage, methodName: String): AsyncJobSingle<ServiceMethodResponse> {
        val rpcEndpoint = getRpcEndpoint(className, methodName)

        return sendMessageOrNotification(rpcEndpoint, message, false)!!
    }

    /**
     * Sends a notification.
     *
     * @param message    The message to send.
     * @param methodName The Target Job Name.
     */
    fun sendNotification(message: GeneratedMessage, methodName: String) {
        val rpcEndpoint = getRpcEndpoint(className, methodName)

        sendMessageOrNotification(rpcEndpoint, message, true)
    }

    private fun sendMessageOrNotification(
        rpcName: String,
        message: GeneratedMessage,
        isNotification: Boolean,
    ): AsyncJobSingle<ServiceMethodResponse>? {
        if (isNotification) {
            steamUnifiedMessages.sendNotification(rpcName, message)
            return null
        }

        return steamUnifiedMessages.sendMessage(rpcName, message)
    }

    companion object {
        // val logger = LogManager.getLogger(UnifiedService.class)

        /**
         * @param parentClassName The parent class name, ie: Player
         * @param methodName      The calling method name, ie: GetGameBadgeLevels
         * @return The name of the RPC endpoint as formatted ServiceName.RpcName. ie: Player.GetGameBadgeLevels#1
         */
        private fun getRpcEndpoint(parentClassName: String, methodName: String): String =
            String.format("%s.%s#%s", parentClassName, methodName, 1)
    }
}
