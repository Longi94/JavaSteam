package `in`.dragonbra.javasteam.steam.handlers.steammatchmaking

import `in`.dragonbra.javasteam.types.SteamID
import java.util.concurrent.ConcurrentHashMap

/**
 * Cache for managing Steam lobbies.
 *
 * @author Lossy
 * @since 2025-05-21
 */
@Suppress("unused")
class LobbyCache {

    private val lobbies: ConcurrentHashMap<Int, ConcurrentHashMap<SteamID, Lobby>> = ConcurrentHashMap()

    fun getLobby(appId: Int, lobbySteamId: Long): Lobby? = getLobby(appId, SteamID(lobbySteamId))

    fun getLobby(appId: Int, lobbySteamId: SteamID): Lobby? = getAppLobbies(appId)[lobbySteamId]

    fun cacheLobby(appId: Int, lobby: Lobby) {
        getAppLobbies(appId)[lobby.steamID] = lobby
    }

    fun addLobbyMember(appId: Int, lobby: Lobby, memberId: Long, personaName: String): Member? = addLobbyMember(appId, lobby, SteamID(memberId), personaName)

    fun addLobbyMember(appId: Int, lobby: Lobby, memberId: SteamID, personaName: String): Member? {
        val existingMember = lobby.members.firstOrNull { it.steamID == memberId }

        if (existingMember != null) {
            // Already in lobby
            return null
        }

        val addedMember = Member(steamID = memberId, personaName = personaName)

        val members = ArrayList<Member>(lobby.members.size + 1)
        members.addAll(lobby.members)
        members.add(addedMember)

        updateLobbyMembers(appId = appId, lobby = lobby, members = members)

        return addedMember
    }

    fun removeLobbyMember(appId: Int, lobby: Lobby, memberId: Long): Member? = removeLobbyMember(appId, lobby, SteamID(memberId))

    fun removeLobbyMember(appId: Int, lobby: Lobby, memberId: SteamID): Member? {
        val removedMember = lobby.members.firstOrNull { it.steamID == memberId }

        if (removedMember == null) {
            return null
        }

        val members = lobby.members.filter { it != removedMember }

        if (members.isNotEmpty()) {
            updateLobbyMembers(appId = appId, lobby = lobby, members = members)
        } else {
            // Steam deletes lobbies that contain no members
            deleteLobby(appId = appId, lobbySteamId = lobby.steamID)
        }

        return removedMember
    }

    fun clearLobbyMembers(appId: Int, lobbySteamId: Long) {
        clearLobbyMembers(appId, SteamID(lobbySteamId))
    }

    fun clearLobbyMembers(appId: Int, lobbySteamId: SteamID) {
        val lobby = getLobby(appId = appId, lobbySteamId = lobbySteamId)

        if (lobby != null) {
            updateLobbyMembers(appId = appId, lobby = lobby, owner = null, members = null)
        }
    }

    fun updateLobbyOwner(appId: Int, lobbySteamId: Long, ownerSteamId: Long) {
        updateLobbyOwner(appId = appId, lobbySteamId = SteamID(lobbySteamId), ownerSteamId = SteamID(ownerSteamId))
    }

    fun updateLobbyOwner(appId: Int, lobbySteamId: SteamID, ownerSteamId: SteamID) {
        val lobby = getLobby(appId = appId, lobbySteamId = lobbySteamId)

        if (lobby != null) {
            updateLobbyMembers(appId = appId, lobby = lobby, owner = ownerSteamId, members = lobby.members)
        }
    }

    fun updateLobbyMembers(appId: Int, lobby: Lobby, members: List<Member>) {
        updateLobbyMembers(appId = appId, lobby = lobby, owner = lobby.ownerSteamID, members = members)
    }

    fun clear() {
        lobbies.clear()
    }

    private fun updateLobbyMembers(appId: Int, lobby: Lobby, owner: SteamID?, members: List<Member>?) {
        cacheLobby(
            appId = appId,
            lobby = Lobby(
                steamID = lobby.steamID,
                lobbyType = lobby.lobbyType,
                lobbyFlags = lobby.lobbyFlags,
                ownerSteamID = owner,
                metadata = lobby.metadata,
                maxMembers = lobby.maxMembers,
                numMembers = lobby.numMembers,
                members = members ?: listOf(),
                distance = lobby.distance,
                weight = lobby.weight
            )
        )
    }

    private fun getAppLobbies(appId: Int): ConcurrentHashMap<SteamID, Lobby> =
        lobbies.computeIfAbsent(appId) { ConcurrentHashMap() }

    private fun deleteLobby(appId: Int, lobbySteamId: SteamID): Lobby? {
        val appLobbies = lobbies[appId] ?: return null
        return appLobbies.remove(lobbySteamId)
    }
}
