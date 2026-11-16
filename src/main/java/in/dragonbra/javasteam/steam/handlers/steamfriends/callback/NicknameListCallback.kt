package `in`.dragonbra.javasteam.steam.handlers.steamfriends.callback

import `in`.dragonbra.javasteam.base.ClientMsgProtobuf
import `in`.dragonbra.javasteam.base.IPacketMsg
import `in`.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserverFriends.CMsgClientPlayerNicknameList
import `in`.dragonbra.javasteam.steam.handlers.steamfriends.PlayerNickname
import `in`.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackMsg

/**
 * This callback is fired when the client receives a list of friend nicknames.
 */
class NicknameListCallback(packetMsg: IPacketMsg) : CallbackMsg() {

    /**
     * Gets a value indicating whether this [NicknameListCallback] is an incremental update.
     * If **false**, [nicknames] is the whole list and replaces any nicknames known before.
     */
    val isIncremental: Boolean

    /**
     * Gets a value indicating whether the [nicknames] of this update were removed.
     */
    val isRemoval: Boolean

    /**
     * Gets the list of nicknames
     */
    val nicknames: List<PlayerNickname>

    init {
        val response = ClientMsgProtobuf<CMsgClientPlayerNicknameList.Builder>(
            CMsgClientPlayerNicknameList::class.java,
            packetMsg
        )

        isIncremental = response.body.incremental
        isRemoval = response.body.removal
        nicknames = response.body.nicknamesList.map { PlayerNickname(it) }
    }
}
