package `in`.dragonbra.javasteam.steam.handlers.steamgameserver.callback

import `in`.dragonbra.javasteam.base.ClientMsgProtobuf
import `in`.dragonbra.javasteam.base.IPacketMsg
import `in`.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserverGameservers.CMsgGSStatusReply
import `in`.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackMsg

/**
 * This callback is fired when the game server receives a status reply.
 */
class StatusReplyCallback(packetMsg: IPacketMsg) : CallbackMsg() {

    /**
     * Gets a value indicating whether this game server is VAC secure.
     */
    val isSecure: Boolean

    init {
        val statusReply = ClientMsgProtobuf<CMsgGSStatusReply.Builder>(
            CMsgGSStatusReply::class.java,
            packetMsg
        )

        isSecure = statusReply.body.isSecure
    }
}
