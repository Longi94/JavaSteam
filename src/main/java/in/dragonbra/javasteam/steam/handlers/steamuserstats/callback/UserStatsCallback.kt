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
import `in`.dragonbra.javasteam.util.JavaSteamAddition
import `in`.dragonbra.javasteam.util.stream.MemoryStream

/**
 * This callback is fired in response to [SteamUserStats.getUserStats].
 */
@JavaSteamAddition
class UserStatsCallback(packetMsg: IPacketMsg?) : CallbackMsg() {

    /**
     * Gets the result.
     */
    val result: EResult

    /**
     * The game id of the stats.
     */
    val gameId: Long

    /**
     * The crc of the stats.
     */
    val crcStats: Int

    /**
     * The raw schema in [ByteString].
     */
    val schema: ByteString

    /**
     * A [List] of [Stats].
     */
    val stats: List<Stats>

    /**
     * A [List] of [AchievementBlocks].
     */
    val achievementBlocks: List<AchievementBlocks>

    /**
     * The schema converted to [KeyValue].
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
