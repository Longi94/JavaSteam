package `in`.dragonbra.javasteam.steam.handlers.steamfriends

import `in`.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserver.CMsgClientAMGetPersonaNameHistoryResponse
import java.util.*

/**
 * Represents a name in a name table
 */
class NameInstance(instance: CMsgClientAMGetPersonaNameHistoryResponse.NameTableInstance.NameInstance) {
    /**
     * Gets the name
     */
    val name: String = instance.name

    /**
     * Gets the time stamp this name was first used
     */
    val nameSince: Date = Date(instance.nameSince * 1000L)
}
