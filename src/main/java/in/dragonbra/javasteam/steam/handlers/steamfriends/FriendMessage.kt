package `in`.dragonbra.javasteam.steam.handlers.steamfriends

import `in`.dragonbra.javasteam.types.SteamID
import java.util.*

/**
 * Represents a single Message sent to or received from a friend
 *
 * @param steamID The SteamID of the User that wrote the message.
 * @param unread Whether the message has been read, i.e., is an offline message.
 * @param message The actual message.
 * @param timestamp The time (in UTC) when the message was sent.
 */
class FriendMessage(
    val steamID: SteamID,
    val unread: Boolean,
    val message: String,
    val timestamp: Date,
)
