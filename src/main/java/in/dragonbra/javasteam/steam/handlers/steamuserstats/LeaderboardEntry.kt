package `in`.dragonbra.javasteam.steam.handlers.steamuserstats

import `in`.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserverLbs.CMsgClientLBSGetLBEntriesResponse
import `in`.dragonbra.javasteam.types.SteamID
import `in`.dragonbra.javasteam.types.UGCHandle
import `in`.dragonbra.javasteam.util.log.LogManager
import `in`.dragonbra.javasteam.util.stream.BinaryReader
import `in`.dragonbra.javasteam.util.stream.MemoryStream
import java.io.IOException

/**
 * Represents a single package in this response.
 */
@Suppress("unused")
class LeaderboardEntry(entry: CMsgClientLBSGetLBEntriesResponse.Entry) {

    companion object {
        private val logger = LogManager.getLogger(LeaderboardEntry::class.java)
    }

    /**
     * Gets the [SteamID] for this entry.
     */
    val steamID: SteamID = SteamID(entry.steamIdUser)

    /**
     * @return the global rank for this entry.
     */
    val globalRank: Int = entry.globalRank

    /**
     * @return the score for this entry.
     */
    val score: Int = entry.score

    /**
     * Gets the [UGCHandle] attached to this entry.
     */
    val ugcId: UGCHandle = UGCHandle(entry.ugcId)

    /**
     * Gets the extra game-defined information regarding how the user got that score.
     */
    val details: List<Int>

    init {
        val entryDetails: MutableList<Int> = mutableListOf()

        if (entry.details != null) {
            val ms = MemoryStream(entry.details.toByteArray())
            val br = BinaryReader(ms)

            try {
                while (ms.length - ms.position > 4) {
                    entryDetails.add(br.readInt())
                }
            } catch (e: IOException) {
                logger.error("failed to read details", e)
            }
        }

        details = entryDetails.toList()
    }
}
