package `in`.dragonbra.javasteam.steam.handlers.steamunifiedmessages.callback

import com.google.protobuf.AbstractMessage
import `in`.dragonbra.javasteam.base.ClientMsgProtobuf
import `in`.dragonbra.javasteam.base.IPacketMsg
import `in`.dragonbra.javasteam.protobufs.steamclient.SteammessagesBase
import `in`.dragonbra.javasteam.steam.handlers.steamunifiedmessages.SteamUnifiedMessages
import `in`.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackMsg

/**
 * @author Lossy
 * @since 2023-01-04
 *
 *
 * This callback represents a service notification received though [SteamUnifiedMessages].
 */
@Suppress("unused", "MemberVisibilityCanBePrivate")
class ServiceMethodNotification(messageType: Class<out AbstractMessage>, packetMsg: IPacketMsg) : CallbackMsg() {

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
     * Gets the protobuf notification body.
     */
    val body: Any

    /**
     * Gets the client message, See [ClientMsgProtobuf]
     */
    val clientMsg: ClientMsgProtobuf<*> = ClientMsgProtobuf(messageType, packetMsg) // Bounce into generic-land.

    /**
     * Gets the Proto Header, See [SteammessagesBase.CMsgProtoBufHeader]
     */
    val protoHeader: SteammessagesBase.CMsgProtoBufHeader = clientMsg.header.proto.build()

    init {
        // Note: JobID will be -1
        methodName = clientMsg.header.proto.getTargetJobName()
        body = clientMsg.body.build()
    }
}
