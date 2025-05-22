package `in`.dragonbra.javasteam.steam.handlers.steammatchmaking

import `in`.dragonbra.javasteam.types.SteamID

/**
 * Represents a Steam user within a lobby.
 * @param steamID SteamID of the lobby member.
 * @param personaName Steam persona of the lobby member.
 * @param metadata Metadata attached to the lobby member.
 *
 * @author Lossy
 * @since 2025-05-21
 */
data class Member(
    val steamID: SteamID,
    val personaName: String,
    val metadata: Map<String, String> = emptyMap(),
) {
    /**
     * Checks to see if this lobby member is equal to another. Only the SteamID of the lobby member is taken into account.
     * @return true, if obj is [Member] with a matching SteamID. Otherwise, false.
     */
    override fun equals(other: Any?): Boolean {
        if (other is Member) {
            return steamID == other.steamID
        }
        return false
    }

    /**
     * Hash code of the lobby member. Only the SteamID of the lobby member is taken into account.
     * @return The hash code of this lobby member.
     */
    override fun hashCode(): Int = steamID.hashCode()
}
