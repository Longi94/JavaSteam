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

        // Parse the schema first so we can enrich achievement data
        // Some games may have empty or invalid schemas, so handle gracefully
        try {
            if (schema.size() > 0) {
                MemoryStream(schema.toByteArray()).use {
                    schemaKeyValues.tryReadAsBinary(it)
                }
            }
        } catch (e: Exception) {
            // Schema parsing failed, schemaKeyValues will remain empty
            // This is okay - we'll just have achievements without enriched metadata
        }

        // Build achievement blocks as originally provided
        achievementBlocks = resp.achievementBlocksList.map { block ->
            AchievementBlocks(
                achievementId = block.achievementId,
                unlockTime = block.unlockTimeList,
                name = null,
                displayName = null,
                description = null,
                icon = null,
                iconGray = null,
                hidden = false
            )
        }
    }

    /**
     * Expands achievement blocks into individual achievements based on the bit-level structure in the schema.
     * Each achievement block contains multiple achievements as bits (typically 32 per block for base game and DLCs).
     *
     * @return List of individual [AchievementBlocks] with enriched metadata from schema
     */
    fun getExpandedAchievements(): List<AchievementBlocks> {
        val expandedAchievements = mutableListOf<AchievementBlocks>()

        try {
            val stats = schemaKeyValues.get("stats")
            if (stats == KeyValue.INVALID) return achievementBlocks // Return original blocks if schema parsing failed

            // Iterate through each achievement block
            for (block in achievementBlocks) {
                val statBlock = stats.get(block.achievementId.toString())
                val bitsBlock = statBlock?.get("bits")

                if (bitsBlock != null) {
                    // This block has bit-level achievements, expand them
                    for (bitEntry in bitsBlock.children) {
                        val bitIndex = bitEntry.get("bit")?.asInteger() ?: continue
                        val displaySection = bitEntry.get("display")

                        // Extract metadata
                        val name = bitEntry.get("name")?.value
                        val displayName = displaySection?.get("name")?.get("english")?.value
                        val description = displaySection?.get("desc")?.get("english")?.value
                        val icon = displaySection?.get("icon")?.value
                        val iconGray = displaySection?.get("icon_gray")?.value
                        val hidden = displaySection?.get("hidden")?.value == "1"

                        // Get unlock time for this specific bit
                        val unlockTime = if (bitIndex < block.unlockTime.size) {
                            block.unlockTime[bitIndex]
                        } else {
                            0
                        }

                        // Create individual achievement entry
                        expandedAchievements.add(
                            AchievementBlocks(
                                achievementId = block.achievementId * 100 + bitIndex, // Create unique ID
                                unlockTime = listOf(unlockTime), // Single timestamp for this achievement
                                name = name,
                                displayName = displayName,
                                description = description,
                                icon = icon,
                                iconGray = iconGray,
                                hidden = hidden
                            )
                        )
                    }
                } else {
                    // No bits structure, keep the block as-is
                    expandedAchievements.add(block)
                }
            }
        } catch (e: Exception) {
            // If expansion fails, return original blocks
            return achievementBlocks
        }

        return expandedAchievements
    }

    /**
     * Builds a map of achievement ID to metadata from the schema KeyValue.
     */
    private fun buildAchievementMetadataMap(schema: KeyValue): Map<Int, Map<String, String>> {
        val metadataMap = mutableMapOf<Int, Map<String, String>>()

        try {
            val achievements = schema.get("stats")?.get("achievements")

            if (achievements != null) {
                for (achievement in achievements.children) {
                    val idKey = achievement.get("id")
                    if (idKey != null) {
                        val achievementId = idKey.asInteger()
                        val metadata = mutableMapOf<String, String>()

                        achievement.get("name")?.value?.let { metadata["name"] = it }
                        achievement.get("displayName")?.value?.let { metadata["displayName"] = it }
                        achievement.get("description")?.value?.let { metadata["description"] = it }
                        achievement.get("icon")?.value?.let { metadata["icon"] = it }
                        achievement.get("icon_gray")?.value?.let { metadata["iconGray"] = it }
                        achievement.get("hidden")?.value?.let { metadata["hidden"] = it }

                        metadataMap[achievementId] = metadata
                    }
                }
            }
        } catch (e: Exception) {
            // If schema parsing fails, return empty map
        }

        return metadataMap
    }
}
