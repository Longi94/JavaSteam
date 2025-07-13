package `in`.dragonbra.javasteam.steam.handlers.steamuserstats.callback

import com.google.protobuf.ByteString
import `in`.dragonbra.javasteam.base.ClientMsgProtobuf
import `in`.dragonbra.javasteam.base.IPacketMsg
import `in`.dragonbra.javasteam.enums.EResult
import `in`.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserverUserstats.CMsgClientGetUserStatsResponse
import `in`.dragonbra.javasteam.steam.handlers.steamuserstats.AchievementBlocks
import `in`.dragonbra.javasteam.steam.handlers.steamuserstats.Stats
import `in`.dragonbra.javasteam.steam.handlers.steamuserstats.SteamUserStats
import `in`.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackMsg
import `in`.dragonbra.javasteam.types.KeyValue
import `in`.dragonbra.javasteam.util.stream.MemoryStream

// JavaSteam Addition
/**
 * This callback is fired in response to [SteamUserStats.getUserStats].
 */
class UserStatsCallback(packetMsg: IPacketMsg?) : CallbackMsg() {

    /**
     * TODO
     */
    val result: EResult

    /**
     * TODO
     */
    val gameId: Long

    /**
     * TODO
     */
    val crcStats: Int

    /**
     * TODO
     */
    val schema: ByteString

    /**
     * TODO
     */
    val stats: List<Stats>

    /**
     * TODO
     */
    val achievementBlocks: List<AchievementBlocks>

    /**
     * TODO
     */
    val schemaKeyValues: KeyValue = KeyValue()

    init {
        val msg = ClientMsgProtobuf<CMsgClientGetUserStatsResponse.Builder>(
            CMsgClientGetUserStatsResponse::class.java,
            packetMsg
        )
        val resp = msg.body

        jobID = msg.targetJobID
        result = EResult.from(resp.eresult)

        gameId = resp.gameId
        crcStats = resp.crcStats
        schema = resp.schema
        stats = resp.statsList.map {
            Stats(statId = it.statId, statValue = it.statValue)
        }
        achievementBlocks = resp.achievementBlocksList.map {
            AchievementBlocks(achievementId = it.achievementId, unlockTime = it.unlockTimeList)
        }

        MemoryStream(schema.toByteArray()).use {
            schemaKeyValues.tryReadAsBinary(it)
        }
    }
}
