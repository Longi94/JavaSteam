package `in`.dragonbra.javasteam.steam.handlers.steammatchmaking.callback

import `in`.dragonbra.javasteam.steam.handlers.steammatchmaking.Lobby
import `in`.dragonbra.javasteam.steam.handlers.steammatchmaking.SteamMatchmaking
import `in`.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackMsg
import `in`.dragonbra.javasteam.types.JobID

/**
 * This callback is fired in response to [SteamMatchmaking.getLobbyData],
 * as well as whenever Steam sends us updated lobby data.
 *
 * @param appID ID of the app the updated lobby belongs to.
 * @param lobby The lobby that was updated.
 *
 * @author Lossy
 * @since 2025-05-21
 */
class LobbyDataCallback(
    jobID: JobID,
    val appID: Int,
    val lobby: Lobby,
) : CallbackMsg() {
    init {
        this.jobID = jobID
    }
}
