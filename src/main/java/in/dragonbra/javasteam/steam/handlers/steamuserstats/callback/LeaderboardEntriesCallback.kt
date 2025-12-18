package `in`.dragonbra.javasteam.steam.handlers.steamuserstats.callback

import `in`.dragonbra.javasteam.base.ClientMsgProtobuf
import `in`.dragonbra.javasteam.base.IPacketMsg
import `in`.dragonbra.javasteam.enums.EResult
import `in`.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserverLbs.CMsgClientLBSGetLBEntriesResponse
import `in`.dragonbra.javasteam.steam.handlers.steamuserstats.LeaderboardEntry
import `in`.dragonbra.javasteam.steam.handlers.steamuserstats.SteamUserStats
import `in`.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackMsg

/**
 * This callback is fired in response to [SteamUserStats.getLeaderboardEntries].
 */
@Suppress("MemberVisibilityCanBePrivate")
class LeaderboardEntriesCallback(packetMsg: IPacketMsg) : CallbackMsg() {

    /**
     * Gets the result of the request.
     */
    val result: EResult

    /**
     * Gets how many entries there are for requested leaderboard.
     */
    val entryCount: Int

    /**
     * Gets the list of leaderboard entries this response contains.
     */
    val entries: List<LeaderboardEntry>

    init {
        val msg = ClientMsgProtobuf<CMsgClientLBSGetLBEntriesResponse.Builder>(
            CMsgClientLBSGetLBEntriesResponse::class.java,
            packetMsg
        )
        val resp = msg.body

        jobID = msg.targetJobID

        result = EResult.from(resp.eresult)
        entryCount = resp.leaderboardEntryCount

        entries = resp.entriesList.map { LeaderboardEntry(it) }
    }
}
