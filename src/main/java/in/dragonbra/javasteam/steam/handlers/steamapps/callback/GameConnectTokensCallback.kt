package `in`.dragonbra.javasteam.steam.handlers.steamapps.callback

import `in`.dragonbra.javasteam.base.ClientMsgProtobuf
import `in`.dragonbra.javasteam.base.IPacketMsg
import `in`.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserver.CMsgClientGameConnectTokens
import `in`.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackMsg

/**
 * This callback is fired when the client receives a list of game connect tokens.
 */
class GameConnectTokensCallback(packetMsg: IPacketMsg) : CallbackMsg() {

    /**
     * Gets a count of tokens to keep.
     */
    val tokensToKeep: Int

    /**
     * Gets the list of tokens.
     */
    val tokens: List<ByteArray>

    init {
        val gcTokens = ClientMsgProtobuf<CMsgClientGameConnectTokens.Builder>(
            CMsgClientGameConnectTokens::class.java,
            packetMsg
        )
        val msg = gcTokens.body

        tokensToKeep = msg.maxTokensToKeep
        tokens = msg.tokensList.map { it.toByteArray() }
    }
}
