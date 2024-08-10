package `in`.dragonbra.javasteam.steam.handlers.steamfriends.callback

import `in`.dragonbra.javasteam.base.ClientMsgProtobuf
import `in`.dragonbra.javasteam.base.IPacketMsg
import `in`.dragonbra.javasteam.enums.EResult
import `in`.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserverFriends.CMsgClientAddFriendResponse
import `in`.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackMsg
import `in`.dragonbra.javasteam.types.SteamID

/**
 * This callback is fired in response to adding a user to your friends list.
 */
class FriendAddedCallback(packetMsg: IPacketMsg) : CallbackMsg() {

    /**
     * Gets the result of the request.
     */
    val result: EResult

    /**
     * Gets the [SteamID] of the friend that was added.
     */
    val steamID: SteamID

    /**
     * Gets the persona name of the friend.
     */
    val personaName: String

    init {
        val friendResponse = ClientMsgProtobuf<CMsgClientAddFriendResponse.Builder>(
            CMsgClientAddFriendResponse::class.java,
            packetMsg
        )
        val msg = friendResponse.body

        result = EResult.from(msg.eresult)

        steamID = SteamID(msg.steamIdAdded)

        personaName = msg.personaNameAdded
    }
}
