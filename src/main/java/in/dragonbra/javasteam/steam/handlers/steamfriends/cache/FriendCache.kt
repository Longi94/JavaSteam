package `in`.dragonbra.javasteam.steam.handlers.steamfriends.cache

import `in`.dragonbra.javasteam.enums.EClanRelationship
import `in`.dragonbra.javasteam.enums.EFriendRelationship
import `in`.dragonbra.javasteam.enums.EPersonaState
import `in`.dragonbra.javasteam.enums.EPersonaStateFlag
import `in`.dragonbra.javasteam.types.GameID
import `in`.dragonbra.javasteam.types.SteamID
import java.util.EnumSet
import java.util.concurrent.*

abstract class Account {
    var steamID: SteamID = SteamID()
    var name: String? = null
    var avatarHash: ByteArray? = null
}

data class User(
    var relationship: EFriendRelationship? = null,
    var personaState: EPersonaState = EPersonaState.Offline,
    var personaStateFlags: EnumSet<EPersonaStateFlag>? = null,
    var gameAppID: Int = 0,
    var gameID: GameID = GameID(),
    var gameName: String? = null,
) : Account()

class Clan(
    var relationship: EClanRelationship = EClanRelationship.None,
) : Account()

class AccountList<T : Account>(private val clazz: Class<T>) : ConcurrentHashMap<SteamID, T>() {
    fun getAccount(id: SteamID): T = getOrPut(id) {
        clazz.getDeclaredConstructor().newInstance().apply {
            steamID = id
        }
    }
}

@Suppress("MemberVisibilityCanBePrivate")
class AccountCache {

    val localUser: User = User()

    val users: AccountList<User> = AccountList(User::class.java)

    val clans: AccountList<Clan> = AccountList(Clan::class.java)

    fun getUser(steamId: SteamID): User = if (isLocalUser(steamId)) localUser else users.getAccount(steamId)

    fun isLocalUser(steamId: SteamID): Boolean = localUser.steamID == steamId
}
