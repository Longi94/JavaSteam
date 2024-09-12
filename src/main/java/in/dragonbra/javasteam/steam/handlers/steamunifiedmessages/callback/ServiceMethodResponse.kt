package `in`.dragonbra.javasteam.steam.handlers.steamunifiedmessages.callback

import com.google.protobuf.AbstractMessage
import com.google.protobuf.GeneratedMessage
import `in`.dragonbra.javasteam.base.ClientMsgProtobuf
import `in`.dragonbra.javasteam.base.PacketClientMsgProtobuf
import `in`.dragonbra.javasteam.enums.EResult
import `in`.dragonbra.javasteam.protobufs.steamclient.SteammessagesBase
import `in`.dragonbra.javasteam.steam.handlers.steamunifiedmessages.SteamUnifiedMessages
import `in`.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackMsg
import `in`.dragonbra.javasteam.types.JobID

/**
 * @author Lossy
 * @since 2023-01-04
 *
 * This callback is returned in response to a service method sent through [SteamUnifiedMessages].
 */
@Suppress("unused", "MemberVisibilityCanBePrivate")
class ServiceMethodResponse(packetMsg: PacketClientMsgProtobuf) : CallbackMsg() {

    /**
     * Gets the result of the message.
     */
    val result: EResult

    /**
     * Gets the name of the Service.
     */
    val serviceName: String
        get() = methodName.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0]

    /**
     * Gets the name of the RPC method.
     */
    val rpcName: String
        get() = methodName.substring(serviceName.length + 1)
            .split("#".toRegex())
            .dropLastWhile { it.isEmpty() }
            .toTypedArray()[0]

    /**
     * Gets the full name of the service method.
     */
    val methodName: String

    /**
     * Gets the packet message, See [PacketClientMsgProtobuf]
     */
    val packetMsg: PacketClientMsgProtobuf

    /**
     * Gets the Proto Header, See [SteammessagesBase.CMsgProtoBufHeader]
     */
    val protoHeader: SteammessagesBase.CMsgProtoBufHeader = packetMsg.header.proto.build()

    init {
        jobID = JobID(protoHeader.jobidTarget)

        result = EResult.from(protoHeader.eresult)
        methodName = protoHeader.targetJobName
        this.packetMsg = packetMsg
    }

    /**
     * Deserializes the response into a protobuf object.
     *
     * @param clazz The message class, type erasure.
     * @param T Protobuf type of the response message.
     * @return The response to the message sent through [SteamUnifiedMessages].
     */
    fun <T : GeneratedMessage.Builder<T>> getDeserializedResponse(clazz: Class<out AbstractMessage>): T {
        val msg = ClientMsgProtobuf<T>(clazz, packetMsg)
        return msg.body
    }
}
