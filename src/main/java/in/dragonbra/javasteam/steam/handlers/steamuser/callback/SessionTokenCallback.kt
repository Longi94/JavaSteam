package `in`.dragonbra.javasteam.steam.handlers.steamuser.callback

import `in`.dragonbra.javasteam.base.ClientMsgProtobuf
import `in`.dragonbra.javasteam.base.IPacketMsg
import `in`.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserver.CMsgClientSessionToken
import `in`.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackMsg

/**
 * This callback is fired when the client receives its unique Steam3 session token.
 * This token is used for authenticated content downloading in Steam2.
 */
class SessionTokenCallback(packetMsg: IPacketMsg) : CallbackMsg() {

    /**
     * @return the Steam3 session token used for authenticating to various other services.
     */
    val sessionToken: Long

    init {
        val sessToken = ClientMsgProtobuf<CMsgClientSessionToken.Builder>(CMsgClientSessionToken::class.java, packetMsg)

        sessionToken = sessToken.body.token
    }
}
