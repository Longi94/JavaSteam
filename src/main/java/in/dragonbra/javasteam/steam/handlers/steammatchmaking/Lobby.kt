package `in`.dragonbra.javasteam.steam.handlers.steammatchmaking

import com.google.protobuf.ByteString
import `in`.dragonbra.javasteam.enums.ELobbyType
import `in`.dragonbra.javasteam.types.KeyValue
import `in`.dragonbra.javasteam.types.SteamID
import `in`.dragonbra.javasteam.util.stream.MemoryStream

/**
 * Represents a Steam lobby.
 * @param steamID SteamID of the lobby.
 * @param lobbyType The type of the lobby.
 * @param lobbyFlags The lobby's flags.
 * @param ownerSteamID The SteamID of the lobby's owner. Please keep in mind that Steam does not provide lobby
 *  owner details for lobbies returned in a lobby list. As such, lobbies that have been
 *  obtained/updated as a result of calling [SteamMatchmaking.getLobbyList]
 *  may have a null (or non-null but state) owner.
 * @param metadata The metadata of the lobby; string key-value pairs.
 * @param maxMembers The maximum number of members that can occupy the lobby.
 * @param numMembers The number of members that are currently occupying the lobby.
 * @param members A list of lobby members. This will only be populated for the user's current lobby.
 * @param distance The distance of the lobby.
 * @param weight The weight of the lobby.
 *
 * @author Lossy
 * @since 2025-05-21
 */
class Lobby(
    val steamID: SteamID,
    val lobbyType: ELobbyType,
    val lobbyFlags: Int,
    val ownerSteamID: SteamID?,
    val metadata: Map<String, String> = mapOf(),
    val maxMembers: Int,
    val numMembers: Int,
    val members: List<Member> = listOf(),
    val distance: Float?,
    val weight: Long?,
) {
    companion object {

        internal fun ByteArray.toByteString(): ByteString = ByteString.copyFrom(this)

        @JvmStatic
        internal fun encodeMetadata(metadata: Map<String, String>?): ByteArray {
            val keyValue = KeyValue("")

            metadata?.forEach { entry ->
                keyValue[entry.key] = KeyValue(null, entry.value)
            }

            return MemoryStream().use { ms ->
                keyValue.saveToStream(ms.asOutputStream(), true)
                ms.toByteArray()
            }
        }

        @JvmStatic
        internal fun decodeMetadata(buffer: ByteString?): Map<String, String> = decodeMetadata(buffer?.toByteArray())

        @JvmStatic
        internal fun decodeMetadata(buffer: ByteArray?): Map<String, String> {
            if (buffer == null || buffer.isEmpty()) {
                return emptyMap()
            }

            val keyValue = KeyValue()

            MemoryStream(buffer).use { ms ->
                if (!keyValue.tryReadAsBinary(ms)) {
                    throw NumberFormatException("Lobby metadata is of an unexpected format")
                }
            }

            val metadata = mutableMapOf<String, String>()

            keyValue.children.forEach { value ->
                if (value.name == null || value.value == null) {
                    return metadata
                }

                metadata[value.name] = value.value
            }

            return metadata.toMap()
        }
    }
}
