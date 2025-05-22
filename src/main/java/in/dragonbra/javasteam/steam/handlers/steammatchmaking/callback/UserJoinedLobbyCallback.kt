package `in`.dragonbra.javasteam.steam.handlers.steammatchmaking.callback

import `in`.dragonbra.javasteam.steam.handlers.steammatchmaking.Member
import `in`.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackMsg
import `in`.dragonbra.javasteam.types.SteamID

/**
 * This callback is fired whenever Steam informs us a user has joined a lobby.
 *
 * @param appID ID of the app the lobby belongs to.
 * @param lobbySteamID The SteamID of the lobby that a member joined.
 * @param user The lobby member that joined.
 *
 * @author Lossy
 * @since 2025-05-21
 */
class UserJoinedLobbyCallback(
    val appID: Int,
    val lobbySteamID: SteamID,
    val user: Member,
) : CallbackMsg() {
    // No job set declared in SK.
}
