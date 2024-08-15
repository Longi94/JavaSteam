package `in`.dragonbra.javasteam.steam.handlers.steamfriends

import `in`.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserver.CMsgClientClanState
import `in`.dragonbra.javasteam.types.GameID
import `in`.dragonbra.javasteam.types.GlobalID
import java.util.*

/**
 * Represents an event or announcement that was posted by a clan.
 */
@Suppress("unused")
class Event(clanEvent: CMsgClientClanState.Event) {

    /**
     * Gets the globally unique ID for this specific event.
     */
    val id: GlobalID = GlobalID(clanEvent.gid)

    /**
     * Gets the event time.
     */
    val eventTime: Date = Date(clanEvent.eventTime * 1000L)

    /**
     * Gets the headline of the event.
     */
    val headline: String = clanEvent.headline

    /**
     * Gets the [GameID] associated with this event, if any.
     */
    val gameID: GameID = GameID(clanEvent.gameId)

    /**
     * Gets a value indicating whether this event was just posted.
     *
     * Gets **true** if the event was just posted; otherwise, **false**.
     */
    val isJustPosted: Boolean = clanEvent.justPosted
}
