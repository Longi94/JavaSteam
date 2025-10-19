package `in`.dragonbra.javasteam.steam.handlers.steamfriends.callback

import `in`.dragonbra.javasteam.enums.EClientPersonaStateFlag
import `in`.dragonbra.javasteam.enums.EPersonaState
import `in`.dragonbra.javasteam.enums.EPersonaStateFlag
import `in`.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserverFriends.CMsgClientPersonaState
import `in`.dragonbra.javasteam.steam.handlers.steamfriends.ClanData
import `in`.dragonbra.javasteam.steam.handlers.steamfriends.KV
import `in`.dragonbra.javasteam.steam.handlers.steamfriends.OtherGameData
import `in`.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackMsg
import `in`.dragonbra.javasteam.types.GameID
import `in`.dragonbra.javasteam.types.SteamID
import `in`.dragonbra.javasteam.util.NetHelpers
import java.net.InetAddress
import java.util.*

/**
 * This callback is fired in response to someone changing their friend details over the network.
 */
@Suppress("unused", "CanBePrimaryConstructorProperty")
class PersonaStateCallback(
    friend: CMsgClientPersonaState.Friend,
    statusFlags: EnumSet<EClientPersonaStateFlag>,
) : CallbackMsg() {

    /**
     * Gets the status flags. This shows what has changed.
     */
    val statusFlags: EnumSet<EClientPersonaStateFlag> = statusFlags

    /**
     * Gets the friend's [SteamID].
     */
    val friendId: SteamID = SteamID(friend.friendid)

    /**
     * Gets the persona state.
     */
    val personaState: EPersonaState = EPersonaState.from(friend.personaState) ?: EPersonaState.Offline

    /**
     * Gets the game app ID being played.
     */
    val gamePlayedAppId: Int = friend.gamePlayedAppId

    /**
     * Gets the game server IP address.
     */
    val gameServerIp: InetAddress = NetHelpers.getIPAddress(friend.gameServerIp)

    /**
     * Gets the game server port.
     */
    val gameServerPort: Int = friend.gameServerPort

    /**
     * Gets the persona state flags.
     */
    val personaStateFlags: EnumSet<EPersonaStateFlag> = EPersonaStateFlag.from(friend.personaStateFlags)

    /**
     * Gets the number of online session instances.
     */
    val onlineSessionInstances: Int = friend.onlineSessionInstances

    /**
     * Gets whether the persona was set by the user.
     */
    val personaSetByUser: Boolean = friend.personaSetByUser

    /**
     * Gets the player name.
     */
    val playerName: String = friend.playerName

    /**
     * Gets the query port.
     */
    val queryPort: Int = friend.queryPort

    /**
     * Gets the source [SteamID].
     */
    val steamIdSource: SteamID = SteamID(friend.steamidSource)

    /**
     * Gets the avatar hash.
     */
    val avatarHash: ByteArray = friend.avatarHash.toByteArray()

    /**
     * Gets the last logoff time.
     */
    val lastLogoff: Date = Date(friend.lastLogoff * 1000L)

    /**
     * Gets the last logon time.
     */
    val lastLogon: Date = Date(friend.lastLogon * 1000L)

    /**
     * Gets the last seen online time.
     */
    val lastSeenOnline: Date = Date(friend.lastSeenOnline * 1000L)

    /**
     * Gets the clan rank.
     */
    val clanRank: Int = friend.clanRank

    /**
     * Gets the name of the game.
     */
    val gameName: String = friend.gameName

    /**
     * Gets the game ID.
     */
    val gameId: GameID = GameID(friend.gameid)

    /**
     * Gets the game data blob.
     */
    val gameDataBlob: ByteArray = friend.gameDataBlob.toByteArray()

    /**
     * Gets the clan data.
     */
    val clanData: ClanData = ClanData(friend.clanData.oggAppId, friend.clanData.chatGroupId)

    /**
     * Gets the clan tag.
     */
    val clanTag: String = friend.clanTag

    /**
     * Gets the rich presence key-value pairs.
     */
    val richPresence: List<KV> = friend.richPresenceList.map { KV(key = it.key, value = it.value) }

    /**
     * Gets the broadcast ID.
     */
    val broadcastId: Long = friend.broadcastId

    /**
     * Gets the game lobby ID.
     */
    val gameLobbyId: Long = friend.gameLobbyId

    /**
     * Gets the account ID of the broadcast being watched.
     */
    val watchingBroadcastAccountId: Int = friend.watchingBroadcastAccountid

    /**
     * Gets the app ID of the broadcast being watched.
     */
    val watchingBroadcastAppId: Int = friend.watchingBroadcastAppid

    /**
     * Gets the number of viewers watching the broadcast.
     */
    val watchingBroadcastViewers: Int = friend.watchingBroadcastViewers

    /**
     * Gets the title of the broadcast being watched.
     */
    val watchingBroadcastTitle: String = friend.watchingBroadcastTitle

    /**
     * Gets whether the user is community banned.
     */
    val isCommunityBanned: Boolean = friend.isCommunityBanned

    /**
     * Gets whether the player name is pending review.
     */
    val playerNamePendingReview: Boolean = friend.playerNamePendingReview

    /**
     * Gets whether the avatar is pending review.
     */
    val avatarPendingReview: Boolean = friend.avatarPendingReview

    /**
     * Gets whether the user is on Steam Deck.
     */
    val onSteamDeck: Boolean = friend.onSteamDeck

    /**
     * Gets the other game data.
     */
    val otherGameData: List<OtherGameData> = friend.otherGameDataList.map { gameData ->
        OtherGameData(
            gameId = gameData.gameid,
            richPresence = gameData.richPresenceList.map { KV(key = it.key, value = it.value) }
        )
    }

    /**
     * Gets the gaming device type.
     */
    val gamingDeviceType: Int = friend.gamingDeviceType
}
