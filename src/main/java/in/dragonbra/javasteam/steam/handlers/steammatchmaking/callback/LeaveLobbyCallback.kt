package `in`.dragonbra.javasteam.steam.handlers.steammatchmaking.callback

import `in`.dragonbra.javasteam.enums.EResult
import `in`.dragonbra.javasteam.steam.handlers.steammatchmaking.SteamMatchmaking
import `in`.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackMsg
import `in`.dragonbra.javasteam.types.JobID
import `in`.dragonbra.javasteam.types.SteamID

/**
 * This callback is fired in response to [SteamMatchmaking.leaveLobby].
 *
 * @param appID ID of the app the targeted lobby belongs to.
 * @param result The result of the request.
 * @param lobbySteamID The SteamID of the targeted Lobby.
 *
 * @author Lossy
 * @since 2025-05-21
 */
class LeaveLobbyCallback(
    jobID: JobID,
    val appID: Int,
    val result: EResult,
    val lobbySteamID: SteamID,
) : CallbackMsg() {
    init {
        this.jobID = jobID
    }
}
