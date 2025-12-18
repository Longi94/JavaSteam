package `in`.dragonbra.javasteam.steam.handlers.steamcontent

import `in`.dragonbra.javasteam.util.JavaSteamAddition

/**
 * Information about a depot patch between two manifest versions.
 * @param isAvailable Whether a patch is available for this upgrade path.
 * @param patchSize Size of the patch file in bytes (delta to download).
 * @param patchedChunksSize Total size in bytes of content that will be patched/modified.
 */
@JavaSteamAddition
data class DepotPatchInfo(
    val isAvailable: Boolean,
    val patchSize: Long,
    val patchedChunksSize: Long,
) {
    override fun toString(): String = "DepotPatchInfo(isAvailable=$isAvailable, patchSize=$patchSize, patchedChunksSize=$patchedChunksSize)"
}
