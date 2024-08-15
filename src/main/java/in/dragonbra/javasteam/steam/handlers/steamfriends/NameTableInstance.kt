package `in`.dragonbra.javasteam.steam.handlers.steamfriends

import `in`.dragonbra.javasteam.enums.EResult
import `in`.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserver.CMsgClientAMGetPersonaNameHistoryResponse
import `in`.dragonbra.javasteam.types.SteamID

/**
 * Represents a name table of an account.
 */
class NameTableInstance(instance: CMsgClientAMGetPersonaNameHistoryResponse.NameTableInstance) {

    /**
     * Gets the result of querying this name table
     */
    val result: EResult = EResult.from(instance.eresult)

    /**
     * Gets the steam id this name table belongs to
     */
    val steamID: SteamID = SteamID(instance.steamid)

    /**
     * Gets the names in this name table
     */
    var names: List<NameInstance> = instance.namesList.map { NameInstance(it) }
}
