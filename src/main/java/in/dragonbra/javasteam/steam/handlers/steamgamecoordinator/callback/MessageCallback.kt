package `in`.dragonbra.javasteam.steam.handlers.steamgamecoordinator.callback

import `in`.dragonbra.javasteam.base.ClientMsgProtobuf
import `in`.dragonbra.javasteam.base.IPacketGCMsg
import `in`.dragonbra.javasteam.base.IPacketMsg
import `in`.dragonbra.javasteam.base.PacketClientGCMsg
import `in`.dragonbra.javasteam.base.PacketClientGCMsgProtobuf
import `in`.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserver2.CMsgGCClient
import `in`.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackMsg
import `in`.dragonbra.javasteam.util.MsgUtil

/**
 * This callback is fired when a game coordinator message is received from the network.
 */
class MessageCallback(packetMsg: IPacketMsg) : CallbackMsg() {

    /**
     * raw emsg (with protobuf flag, if present)
     */
    private val _eMsg: Int

    /**
     * Gets the game coordinator message type.
     */
    val eMsg: Int
        get() = MsgUtil.getGCMsg(_eMsg)

    /**
     * Gets the AppID of the game coordinator the message is from.
     */
    val appID: Int

    /**
     * Gets a value indicating whether this message is protobuf'd.
     */
    val isProto: Boolean
        get() = MsgUtil.isProtoBuf(_eMsg)

    /**
     * Gets the actual message.
     */
    val message: IPacketGCMsg

    init {
        val msg = ClientMsgProtobuf<CMsgGCClient.Builder>(
            CMsgGCClient::class.java,
            packetMsg
        )
        val gcMsg = msg.body

        _eMsg = gcMsg.msgtype
        appID = gcMsg.appid
        message = getPacketGCMsg(gcMsg.msgtype, gcMsg.payload.toByteArray())
        jobID = message.targetJobID
    }

    companion object {
        private fun getPacketGCMsg(eMsg: Int, data: ByteArray): IPacketGCMsg {
            val realEMsg: Int = MsgUtil.getGCMsg(eMsg)

            return if (MsgUtil.isProtoBuf(eMsg)) {
                PacketClientGCMsgProtobuf(realEMsg, data)
            } else {
                PacketClientGCMsg(realEMsg, data)
            }
        }
    }
}
