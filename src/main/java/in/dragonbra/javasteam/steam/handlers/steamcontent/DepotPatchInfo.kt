package `in`.dragonbra.javasteam.steam.handlers.steamcontent

import `in`.dragonbra.javasteam.util.JavaSteamAddition

/**
 * TODO kdoc
 * @param isAvailable
 * @param patchSize
 * @param patchedChunksSize
 */
@JavaSteamAddition
data class DepotPatchInfo(
    val isAvailable: Boolean,
    val patchSize: Long,
    val patchedChunksSize: Long,
)
