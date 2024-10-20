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
import `in`.dragonbra.javasteam.types.JobID
import `in`.dragonbra.javasteam.util.log.LogManager
import `in`.dragonbra.javasteam.util.log.Logger
import java.lang.reflect.Method

/**
 * @author Lossy
 * @since 2023-01-04
 *
 * This handler is used for interacting with Steamworks unified messaging
 */
class SteamUnifiedMessages : ClientMsgHandler() {

    /**
     * Handles a client message. This should not be called directly.
     *
     * @param packetMsg The packet message that contains the data.
     */
    override fun handleMsg(packetMsg: IPacketMsg) {
        when (packetMsg.msgType) {
            EMsg.ServiceMethodResponse -> handleServiceMethodResponse(packetMsg)
            EMsg.ServiceMethod -> handleServiceMethod(packetMsg)
            else -> Unit
        }
    }

    /**
     * Sends a message.
     * Results are returned in a [ServiceMethodResponse].
     *
     * @param rpcName Name of the RPC endpoint. Takes the format ServiceName.RpcName
     * @param message The message to send.
     * @param TRequest The type of protobuf object.
     * @return The JobID of the request. This can be used to find the appropriate [ServiceMethodResponse].
     */
    fun <TRequest : GeneratedMessage.Builder<TRequest>> sendMessage(
        rpcName: String,
        message: GeneratedMessage,
    ): AsyncJobSingle<ServiceMethodResponse> {
        val jobID: JobID = client.getNextJobID()
        val eMsg = if (client.steamID == null) {
            EMsg.ServiceMethodCallFromClientNonAuthed
        } else {
            EMsg.ServiceMethodCallFromClient
        }

        ClientMsgProtobuf<TRequest>(message.javaClass, eMsg).apply {
            sourceJobID = jobID
            header.proto.targetJobName = rpcName
            body!!.mergeFrom(message)
        }.also(client::send)

        return AsyncJobSingle(client, jobID)
    }

    /**
     * Sends a notification.
     *
     * @param rpcName Name of the RPC endpoint. Takes the format ServiceName.RpcName
     * @param message The message to send.
     * @param TRequest The type of protobuf object.
     */
    fun <TRequest : GeneratedMessage.Builder<TRequest>> sendNotification(
        rpcName: String,
        message: GeneratedMessage,
    ) {
        val eMsg = if (client.steamID == null) {
            EMsg.ServiceMethodCallFromClientNonAuthed
        } else {
            EMsg.ServiceMethodCallFromClient
        }

        ClientMsgProtobuf<TRequest>(message.javaClass, eMsg).apply {
            header.proto.targetJobName = rpcName
            body!!.mergeFrom(message)
        }.also(client::send)
    }

    private fun handleServiceMethodResponse(packetMsg: IPacketMsg) {
        require(packetMsg is PacketClientMsgProtobuf) { "Packet message is expected to be protobuf." }

        val callback = ServiceMethodResponse(packetMsg)
        client.postCallback(callback)
    }

    private fun handleServiceMethod(packetMsg: IPacketMsg) {
        require(packetMsg is PacketClientMsgProtobuf) { "Packet message is expected to be protobuf." }

        val jobName = packetMsg.header.proto.targetJobName

        if (jobName.isNullOrEmpty()) {
            logger.debug("Job name is null or empty")
            return
        }

        val splitByDot = jobName.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val splitByHash = splitByDot[1].split("#".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

        val serviceName = splitByDot[0]
        val methodName = splitByHash[0]

        val serviceInterfaceName = "in.dragonbra.javasteam.rpc.interfaces.I$serviceName"
        try {
            logger.debug("Handling Service Method: $serviceInterfaceName")

            val serviceInterfaceType = Class.forName(serviceInterfaceName)

            var method: Method? = null
            for (m in serviceInterfaceType.declaredMethods) {
                if (m.name == methodName) {
                    method = m
                }
            }

            if (method != null) {
                @Suppress("UNCHECKED_CAST")
                val argumentType = method.parameterTypes[0] as Class<out AbstractMessage>

                client.postCallback(ServiceMethodNotification(argumentType, packetMsg))
            }
        } catch (_: ClassNotFoundException) {
            // The RPC service implementation was not implemented.
            // Either the .proto is missing, or the service was not converted to an interface yet.
            logger.debug("Service Method: $serviceName, was not found")
        }
    }

    companion object {
        private val logger: Logger = LogManager.getLogger(SteamUnifiedMessages::class.java)
    }
}
