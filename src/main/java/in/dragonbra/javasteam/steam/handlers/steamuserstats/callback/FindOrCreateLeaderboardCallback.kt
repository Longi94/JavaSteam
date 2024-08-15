package `in`.dragonbra.javasteam.steam.handlers.steamuserstats.callback

import `in`.dragonbra.javasteam.base.ClientMsgProtobuf
import `in`.dragonbra.javasteam.base.IPacketMsg
import `in`.dragonbra.javasteam.enums.ELeaderboardDisplayType
import `in`.dragonbra.javasteam.enums.ELeaderboardSortMethod
import `in`.dragonbra.javasteam.enums.EResult
import `in`.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserverLbs.CMsgClientLBSFindOrCreateLBResponse
import `in`.dragonbra.javasteam.steam.handlers.steamuserstats.SteamUserStats
import `in`.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackMsg

/**
 * This callback is fired in response to [SteamUserStats.findLeaderBoard] and [SteamUserStats.createLeaderboard].
 */
@Suppress("MemberVisibilityCanBePrivate")
class FindOrCreateLeaderboardCallback(packetMsg: IPacketMsg?) : CallbackMsg() {

    /**
     * Gets the result of the request.
     */
    val result: EResult

    /**
     * Gets the leaderboard ID.
     */
    val id: Int

    /**
     * Gets how many entries there are for requested leaderboard.
     */
    val entryCount: Int

    /**
     * Gets the sort method to use for this leaderboard.
     */
    val sortMethod: ELeaderboardSortMethod

    /**
     * Gets the display type for this leaderboard.
     */
    val displayType: ELeaderboardDisplayType

    init {
        val msg = ClientMsgProtobuf<CMsgClientLBSFindOrCreateLBResponse.Builder>(
            CMsgClientLBSFindOrCreateLBResponse::class.java,
            packetMsg
        )
        val resp = msg.body

        jobID = msg.targetJobID

        result = EResult.from(resp.eresult)
        id = resp.leaderboardId
        entryCount = resp.leaderboardEntryCount
        sortMethod = ELeaderboardSortMethod.from(resp.leaderboardSortMethod)
        displayType = ELeaderboardDisplayType.from(resp.leaderboardDisplayType)
    }
}
