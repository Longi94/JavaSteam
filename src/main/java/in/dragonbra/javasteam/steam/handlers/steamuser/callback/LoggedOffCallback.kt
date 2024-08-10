package `in`.dragonbra.javasteam.steam.handlers.steamuser.callback

import `in`.dragonbra.javasteam.base.ClientMsg
import `in`.dragonbra.javasteam.base.ClientMsgProtobuf
import `in`.dragonbra.javasteam.base.IPacketMsg
import `in`.dragonbra.javasteam.enums.EResult
import `in`.dragonbra.javasteam.generated.MsgClientLoggedOff
import `in`.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserverLogin
import `in`.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackMsg

/**
 * This callback is returned when the client is told to log off by the server.
 */
class LoggedOffCallback(packetMsg: IPacketMsg) : CallbackMsg() {

    /**
     * Gets the result of the log off.
     */
    val result: EResult

    init {
        if (packetMsg.isProto) {
            val loggedOff = ClientMsgProtobuf<SteammessagesClientserverLogin.CMsgClientLoggedOff.Builder>(
                SteammessagesClientserverLogin.CMsgClientLoggedOff::class.java,
                packetMsg
            )
            result = EResult.from(loggedOff.body.eresult)
        } else {
            val loggedOff = ClientMsg(MsgClientLoggedOff::class.java, packetMsg)
            result = loggedOff.body.result
        }
    }
}
