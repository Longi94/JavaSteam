package `in`.dragonbra.javasteam.steam.handlers.steamunifiedmessages.callback

import com.google.protobuf.AbstractMessage
import com.google.protobuf.GeneratedMessage
import `in`.dragonbra.javasteam.base.ClientMsgProtobuf
import `in`.dragonbra.javasteam.base.PacketClientMsgProtobuf
import `in`.dragonbra.javasteam.steam.handlers.steamunifiedmessages.SteamUnifiedMessages
import `in`.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackMsg
import `in`.dragonbra.javasteam.types.JobID

/**
 * @author Lossy
 * @since 2024-10-22
 *
 * This callback represents a service notification received though [SteamUnifiedMessages].
 */
@Suppress("MemberVisibilityCanBePrivate")
class ServiceMethodNotification<T : GeneratedMessage.Builder<T>>(
    clazz: Class<out AbstractMessage>,
    packetMsg: PacketClientMsgProtobuf,
) : CallbackMsg() {

    /**
     * The name of the job, in the format Service.Method#Version.
     */
    val jobName: String

    /**
     * The protobuf body.
     */
    val body: T

    init {
        jobID = JobID.INVALID
        jobName = packetMsg.header.proto.targetJobName
        body = ClientMsgProtobuf<T>(clazz, packetMsg).body
    }
}
