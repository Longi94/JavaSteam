package `in`.dragonbra.javasteam.steam.handlers.steamcontent

/**
 * TODO kdoc
 * @param appIds
 * @param ipPublic
 */
data class GetPeerContentInfo(
    val appIds: List<Int>,
    val ipPublic: String,
)
