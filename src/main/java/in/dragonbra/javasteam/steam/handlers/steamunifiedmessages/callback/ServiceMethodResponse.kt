package `in`.dragonbra.javasteam.steam.handlers.steamunifiedmessages.callback

import com.google.protobuf.AbstractMessage
import com.google.protobuf.GeneratedMessage
import `in`.dragonbra.javasteam.base.ClientMsgProtobuf
import `in`.dragonbra.javasteam.base.PacketClientMsgProtobuf
import `in`.dragonbra.javasteam.enums.EResult
import `in`.dragonbra.javasteam.steam.handlers.steamunifiedmessages.SteamUnifiedMessages
import `in`.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackMsg
import `in`.dragonbra.javasteam.types.JobID

/**
 * @author Lossy
 * @since 2024-10-22
 *
 * This callback is returned in response to a service method sent through [SteamUnifiedMessages].
 */
@Suppress("MemberVisibilityCanBePrivate")
class ServiceMethodResponse<T : GeneratedMessage.Builder<T>>(
    clazz: Class<out AbstractMessage>,
    packetMsg: PacketClientMsgProtobuf,
) : CallbackMsg() {

    /**
     * The result of the message.
     */
    val result: EResult

    /**
     * The protobuf body.
     */
    val body: T

    init {
        val protoHeader = packetMsg.header.proto
        jobID = JobID(protoHeader.jobidTarget)
        result = EResult.from(protoHeader.eresult)
        body = ClientMsgProtobuf<T>(clazz, packetMsg).body
    }
}
