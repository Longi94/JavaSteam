package `in`.dragonbra.javasteam.steam.handlers.steamuserstats

import `in`.dragonbra.javasteam.base.ClientMsgProtobuf
import `in`.dragonbra.javasteam.base.IPacketMsg
import `in`.dragonbra.javasteam.enums.ELeaderboardDataRequest
import `in`.dragonbra.javasteam.enums.ELeaderboardDisplayType
import `in`.dragonbra.javasteam.enums.ELeaderboardSortMethod
import `in`.dragonbra.javasteam.enums.EMsg
import `in`.dragonbra.javasteam.handlers.ClientMsgHandler
import `in`.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserver2.CMsgDPGetNumberOfCurrentPlayers
import `in`.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserverLbs.CMsgClientLBSFindOrCreateLB
import `in`.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserverLbs.CMsgClientLBSGetLBEntries
import `in`.dragonbra.javasteam.steam.handlers.steamuserstats.callback.FindOrCreateLeaderboardCallback
import `in`.dragonbra.javasteam.steam.handlers.steamuserstats.callback.LeaderboardEntriesCallback
import `in`.dragonbra.javasteam.steam.handlers.steamuserstats.callback.NumberOfPlayersCallback
import `in`.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackMsg
import `in`.dragonbra.javasteam.types.AsyncJobSingle

/**
 * This handler handles Steam user statistic related actions.
 */
class SteamUserStats : ClientMsgHandler() {

    /**
     * Retrieves the number of current players for a given app id.
     * Results are returned in a [NumberOfPlayersCallback].
     *
     * @param appId The app id to request the number of players for.
     * @return The Job ID of the request. This can be used to find the appropriate [NumberOfPlayersCallback].
     */
    fun getNumberOfCurrentPlayers(appId: Int): AsyncJobSingle<NumberOfPlayersCallback> {
        val msg = ClientMsgProtobuf<CMsgDPGetNumberOfCurrentPlayers.Builder>(
            CMsgDPGetNumberOfCurrentPlayers::class.java,
            EMsg.ClientGetNumberOfCurrentPlayersDP
        )
        msg.setSourceJobID(client.getNextJobID())

        msg.body.setAppid(appId)

        client.send(msg)

        return AsyncJobSingle(this.client, msg.sourceJobID)
    }

    /**
     * Asks the Steam back-end for a leaderboard by name for a given appid.
     * Results are returned in a [FindOrCreateLeaderboardCallback].
     * The returned [AsyncJobSingle] can also be awaited to retrieve the callback result.
     *
     * @param appId The AppID to request a leaderboard for.
     * @param name  Name of the leaderboard to request.
     * @return The Job ID of the request. This can be used to find the appropriate [FindOrCreateLeaderboardCallback].
     */
    fun findLeaderBoard(appId: Int, name: String?): AsyncJobSingle<FindOrCreateLeaderboardCallback> {
        val msg = ClientMsgProtobuf<CMsgClientLBSFindOrCreateLB.Builder>(
            CMsgClientLBSFindOrCreateLB::class.java,
            EMsg.ClientLBSFindOrCreateLB
        )
        msg.setSourceJobID(client.getNextJobID())

        // routing_appid has to be set correctly to receive a response
        msg.protoHeader.setRoutingAppid(appId)

        msg.body.setAppId(appId)
        msg.body.setLeaderboardName(name)
        msg.body.setCreateIfNotFound(false)

        client.send(msg)

        return AsyncJobSingle(this.client, msg.sourceJobID)
    }

    /**
     * Asks the Steam back-end for a leaderboard by name, and will create it if it's not yet.
     * Results are returned in a [FindOrCreateLeaderboardCallback].
     * The returned [AsyncJobSingle] can also be awaited to retrieve the callback result.
     *
     * @param appId       The AppID to request a leaderboard for.
     * @param name        Name of the leaderboard to create.
     * @param sortMethod  Sort method to use for this leaderboard
     * @param displayType Display type for this leaderboard.
     * @return The Job ID of the request. This can be used to find the appropriate [FindOrCreateLeaderboardCallback].
     */
    fun createLeaderboard(
        appId: Int,
        name: String,
        sortMethod: ELeaderboardSortMethod,
        displayType: ELeaderboardDisplayType,
    ): AsyncJobSingle<FindOrCreateLeaderboardCallback> {
        val msg = ClientMsgProtobuf<CMsgClientLBSFindOrCreateLB.Builder>(
            CMsgClientLBSFindOrCreateLB::class.java,
            EMsg.ClientLBSFindOrCreateLB
        )
        msg.setSourceJobID(client.getNextJobID())

        // routing_appid has to be set correctly to receive a response
        msg.protoHeader.setRoutingAppid(appId)

        msg.body.setAppId(appId)
        msg.body.setLeaderboardName(name)
        msg.body.setLeaderboardDisplayType(displayType.code())
        msg.body.setLeaderboardSortMethod(sortMethod.code())
        msg.body.setCreateIfNotFound(true)

        client.send(msg)

        return AsyncJobSingle(this.client, msg.sourceJobID)
    }

    /**
     * Asks the Steam back-end for a set of rows in the leaderboard.
     * Results are returned in a [LeaderboardEntriesCallback].
     * The returned [AsyncJobSingle] can also be awaited to retrieve the callback result.
     *
     * @param appId       The AppID to request leaderboard rows for.
     * @param id          ID of the leaderboard to view.
     * @param rangeStart  Range start or 0.
     * @param rangeEnd    Range end or max leaderboard entries.
     * @param dataRequest Type of request.
     * @return The Job ID of the request. This can be used to find the appropriate [LeaderboardEntriesCallback].
     */
    fun getLeaderboardEntries(
        appId: Int,
        id: Int,
        rangeStart: Int,
        rangeEnd: Int,
        dataRequest: ELeaderboardDataRequest,
    ): AsyncJobSingle<LeaderboardEntriesCallback> {
        val msg = ClientMsgProtobuf<CMsgClientLBSGetLBEntries.Builder>(
            CMsgClientLBSGetLBEntries::class.java,
            EMsg.ClientLBSGetLBEntries
        )
        msg.setSourceJobID(client.getNextJobID())

        msg.body.setAppId(appId)
        msg.body.setLeaderboardId(id)
        msg.body.setLeaderboardDataRequest(dataRequest.code())
        msg.body.setRangeStart(rangeStart)
        msg.body.setRangeEnd(rangeEnd)

        client.send(msg)

        return AsyncJobSingle(this.client, msg.sourceJobID)
    }

    /**
     * Handles a client message. This should not be called directly.
     * @param packetMsg The packet message that contains the data.
     */
    override fun handleMsg(packetMsg: IPacketMsg) {
        // ignore messages that we don't have a handler function for
        val callback: CallbackMsg = getCallback(packetMsg) ?: return

        client.postCallback(callback)
    }

    companion object {
        private fun getCallback(packetMsg: IPacketMsg): CallbackMsg? = when (packetMsg.getMsgType()) {
            EMsg.ClientGetNumberOfCurrentPlayersDPResponse -> NumberOfPlayersCallback(packetMsg)
            EMsg.ClientLBSFindOrCreateLBResponse -> FindOrCreateLeaderboardCallback(packetMsg)
            EMsg.ClientLBSGetLBEntriesResponse -> LeaderboardEntriesCallback(packetMsg)
            else -> null
        }
    }
}
