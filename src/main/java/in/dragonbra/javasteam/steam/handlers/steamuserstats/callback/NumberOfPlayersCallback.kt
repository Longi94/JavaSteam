package `in`.dragonbra.javasteam.steam.handlers.steamuserstats.callback

import `in`.dragonbra.javasteam.base.ClientMsgProtobuf
import `in`.dragonbra.javasteam.base.IPacketMsg
import `in`.dragonbra.javasteam.enums.EResult
import `in`.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserver2.CMsgDPGetNumberOfCurrentPlayersResponse
import `in`.dragonbra.javasteam.steam.handlers.steamuserstats.SteamUserStats
import `in`.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackMsg

/**
 * This callback is fired in response to [SteamUserStats.getNumberOfCurrentPlayers].
 */
@Suppress("MemberVisibilityCanBePrivate")
class NumberOfPlayersCallback(packetMsg: IPacketMsg?) : CallbackMsg() {

    /**
     * Gets the result of the request.
     */
    val result: EResult

    /**
     * Gets the current number of players according to Steam.
     */
    val numPlayers: Int

    init {
        val msg = ClientMsgProtobuf<CMsgDPGetNumberOfCurrentPlayersResponse.Builder>(
            CMsgDPGetNumberOfCurrentPlayersResponse::class.java,
            packetMsg
        )
        val resp = msg.body

        jobID = msg.targetJobID
        result = EResult.from(resp.eresult)
        numPlayers = resp.playerCount
    }
}
