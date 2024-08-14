package `in`.dragonbra.javasteam.steam.handlers.steamfriends.callback

import `in`.dragonbra.javasteam.base.ClientMsg
import `in`.dragonbra.javasteam.base.IPacketMsg
import `in`.dragonbra.javasteam.enums.EResult
import `in`.dragonbra.javasteam.generated.MsgClientSetIgnoreFriendResponse
import `in`.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackMsg

/**
 * This callback is fired in response to an attempt at ignoring a friend.
 */
class IgnoreFriendCallback(packetMsg: IPacketMsg) : CallbackMsg() {

    /**
     * Gets the result of ignoring a friend.
     */
    val result: EResult

    init {
        val response = ClientMsg(MsgClientSetIgnoreFriendResponse::class.java, packetMsg)

        jobID = response.targetJobID
        result = response.body.result
    }
}
