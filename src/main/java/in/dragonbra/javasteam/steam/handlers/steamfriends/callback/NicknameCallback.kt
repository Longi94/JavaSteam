package `in`.dragonbra.javasteam.steam.handlers.steamfriends.callback

import `in`.dragonbra.javasteam.base.ClientMsgProtobuf
import `in`.dragonbra.javasteam.base.IPacketMsg
import `in`.dragonbra.javasteam.enums.EResult
import `in`.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserverFriends.CMsgClientSetPlayerNicknameResponse
import `in`.dragonbra.javasteam.steam.handlers.steamfriends.SteamFriends
import `in`.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackMsg

/**
 * This callback is fired in response to setting a nickname of a player by calling [SteamFriends.setFriendNickname].
 */
class NicknameCallback(packetMsg: IPacketMsg) : CallbackMsg() {

    /**
     * Gets the result of setting a nickname
     */
    val result: EResult

    init {
        val response = ClientMsgProtobuf<CMsgClientSetPlayerNicknameResponse.Builder>(
            CMsgClientSetPlayerNicknameResponse::class.java,
            packetMsg
        )

        jobID = response.targetJobID
        result = EResult.from(response.body.eresult)
    }
}
