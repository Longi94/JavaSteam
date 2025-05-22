package `in`.dragonbra.javasteam.steam.handlers.steammatchmaking.callback

import `in`.dragonbra.javasteam.enums.EChatRoomEnterResponse
import `in`.dragonbra.javasteam.steam.handlers.steammatchmaking.Lobby
import `in`.dragonbra.javasteam.steam.handlers.steammatchmaking.SteamMatchmaking
import `in`.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackMsg
import `in`.dragonbra.javasteam.types.JobID

/**
 * This callback is fired in response to [SteamMatchmaking.joinLobby].
 *
 * @param appID ID of the app the targeted lobby belongs to.
 * @param chatRoomEnterResponse  The result of the request.
 * @param lobby The joined [Lobby], when [chatRoomEnterResponse] equals [EChatRoomEnterResponse.Success], otherwise <c>null</c>
 *
 * @author Lossy
 * @since 2025-05-21
 */
class JoinLobbyCallback(
    jobID: JobID,
    val appID: Int,
    val chatRoomEnterResponse: EChatRoomEnterResponse,
    val lobby: Lobby?,
) : CallbackMsg() {
    init {
        this.jobID = jobID
    }
}
