package `in`.dragonbra.javasteam.steam.handlers.steamfriends

/**
 * Represents game data for other games a user is playing or has played.
 *
 * @property gameId The game ID.
 * @property richPresence The list of rich presence key-value pairs for the game.
 */
data class OtherGameData(val gameId: Long, val richPresence: List<KV>)
