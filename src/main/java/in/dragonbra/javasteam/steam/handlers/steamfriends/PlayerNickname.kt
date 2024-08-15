package `in`.dragonbra.javasteam.steam.handlers.steamfriends

import `in`.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserverFriends.CMsgClientPlayerNicknameList
import `in`.dragonbra.javasteam.types.SteamID

/**
 * Represents a nickname of a friend
 */
class PlayerNickname(nickname: CMsgClientPlayerNicknameList.PlayerNickname) {

    /**
     * Gets the steam id of the friend
     */
    val steamID: SteamID = SteamID(nickname.steamid)

    /**
     * Gets the nickname of the friend
     */
    val nickname: String = nickname.nickname
}
