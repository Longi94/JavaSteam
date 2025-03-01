package `in`.dragonbra.javasteam.steam.handlers.steamfriends.callback

import `in`.dragonbra.javasteam.enums.EClientPersonaStateFlag
import `in`.dragonbra.javasteam.enums.EPersonaState
import `in`.dragonbra.javasteam.enums.EPersonaStateFlag
import `in`.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserverFriends.CMsgClientPersonaState
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
     * Gets the friend's [SteamID]
     */
    val friendID: SteamID = SteamID(friend.friendid)

    /**
     * Gets the state.
     */
    val state: EPersonaState = EPersonaState.from(friend.personaState) ?: EPersonaState.Offline

    /**
     * Gets the state flags.
     */
    val stateFlags: EnumSet<EPersonaStateFlag> = EPersonaStateFlag.from(friend.personaStateFlags)

    /**
     * Gets the game app ID.
     */
    val gameAppID: Int = friend.gamePlayedAppId

    /**
     * Gets the game ID.
     */
    val gameID: GameID = GameID(friend.gameid)

    /**
     * Gets the name of the game.
     */
    val gameName: String = friend.gameName

    /**
     * Gets the game server IP.
     */
    val gameServerIP: InetAddress = NetHelpers.getIPAddress(friend.gameServerIp)

    /**
     * Gets the game server port.
     */
    val gameServerPort: Int = friend.gameServerPort

    /**
     * Gets the query port.
     */
    val queryPort: Int = friend.queryPort

    /**
     * Gets the source [SteamID].
     */
    val sourceSteamID: SteamID = SteamID(friend.steamidSource)

    /**
     * Gets the game data blob.
     */
    val gameDataBlob: ByteArray = friend.gameDataBlob.toByteArray()

    /**
     * Gets the name.
     */
    val name: String = friend.playerName

    /**
     * Gets the avatar hash.
     */
    val avatarHash: ByteArray = friend.avatarHash.toByteArray()

    /**
     * Gets the last log off.
     */
    val lastLogOff: Date = Date(friend.lastLogoff * 1000L)

    /**
     * Gets the last log on.
     */
    val lastLogOn: Date = Date(friend.lastLogon * 1000L)

    /**
     * Gets the clan rank.
     */
    val clanRank: Int = friend.clanRank

    /**
     * Gets the clan tag.
     */
    val clanTag: String = friend.clanTag

    /**
     * Gets the online session instance.
     */
    val onlineSessionInstances: Int = friend.onlineSessionInstances
}
