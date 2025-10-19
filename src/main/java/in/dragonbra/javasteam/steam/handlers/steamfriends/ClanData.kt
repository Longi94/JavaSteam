package `in`.dragonbra.javasteam.steam.handlers.steamfriends

/**
 * Represents clan-related data for a friend.
 *
 * @property oggAppId The app ID associated with the clan.
 * @property chatGroupID The chat group ID for the clan.
 */
data class ClanData(val oggAppId: Int, val chatGroupID: Long)
