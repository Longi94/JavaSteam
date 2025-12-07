package `in`.dragonbra.javasteam.steam.handlers.steamgamecoordinator

import com.google.protobuf.ByteString
import `in`.dragonbra.javasteam.base.ClientMsgProtobuf
import `in`.dragonbra.javasteam.base.IPacketMsg
import `in`.dragonbra.javasteam.base.gc.IClientGCMsg
import `in`.dragonbra.javasteam.enums.EMsg
import `in`.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserver2.CMsgGCClient
import `in`.dragonbra.javasteam.steam.handlers.ClientMsgHandler
import `in`.dragonbra.javasteam.steam.handlers.steamgamecoordinator.callback.MessageCallback
import `in`.dragonbra.javasteam.util.MsgUtil

/**
 * This handler handles all game coordinator messaging.
 */
class SteamGameCoordinator : ClientMsgHandler() {

    /**
     * Sends a game coordinator message for a specific appid.
     *
     * @param msg   The GC message to send.
     * @param appId The app id of the game coordinator to send to.
     */
    fun send(msg: IClientGCMsg, appId: Int) {
        val clientMsg = ClientMsgProtobuf<CMsgGCClient.Builder>(CMsgGCClient::class.java, EMsg.ClientToGC).apply {
            protoHeader.routingAppid = appId
            body.msgtype = MsgUtil.makeGCMsg(msg.msgType, msg.isProto)
            body.appid = appId

            body.payload = ByteString.copyFrom(msg.serialize())
        }

        client.send(clientMsg)
    }

    /**
     * Handles a client message. This should not be called directly.
     *
     * @param packetMsg The packet message that contains the data.
     */
    override fun handleMsg(packetMsg: IPacketMsg) {
        if (packetMsg.msgType == EMsg.ClientFromGC) {
            val callback = MessageCallback(packetMsg)
            client.postCallback(callback)
        }
    }
}
