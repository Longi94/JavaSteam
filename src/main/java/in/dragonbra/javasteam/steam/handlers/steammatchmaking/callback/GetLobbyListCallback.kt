package `in`.dragonbra.javasteam.steam.handlers.steammatchmaking.callback

import `in`.dragonbra.javasteam.enums.EResult
import `in`.dragonbra.javasteam.steam.handlers.steammatchmaking.Lobby
import `in`.dragonbra.javasteam.steam.handlers.steammatchmaking.SteamMatchmaking
import `in`.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackMsg
import `in`.dragonbra.javasteam.types.JobID

/**
 * This callback is fired in response to [SteamMatchmaking.getLobbyList]
 *
 * @param appID ID of the app the lobbies belongs to.
 * @param result The result of the request.
 * @param lobbies The list of lobbies matching the criteria specified with [SteamMatchmaking.getLobbyList].
 *
 * @author Lossy
 * @since 2025-05-21
 */
class GetLobbyListCallback(
    jobID: JobID,
    val appID: Int,
    val result: EResult,
    val lobbies: List<Lobby>,
) : CallbackMsg() {
    init {
        this.jobID = jobID
    }
}
