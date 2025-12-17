package `in`.dragonbra.javasteam.steam.handlers.steamcontent

/**
 * TODO kdoc
 * @param isAvailable
 * @param patchSize
 * @param patchedChunksSize
 */
data class DepotPatchInfo(
    val isAvailable: Boolean,
    val patchSize: Long,
    val patchedChunksSize: Long,
)
