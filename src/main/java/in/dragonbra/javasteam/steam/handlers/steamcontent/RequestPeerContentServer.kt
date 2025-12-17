package `in`.dragonbra.javasteam.steam.handlers.steamcontent

/**
 * TODO kdoc
 * @param serverPort
 * @param installedDepots
 * @param accessToken
 */
data class RequestPeerContentServer(
    val serverPort: Int,
    val installedDepots: List<Int>,
    val accessToken: Long,
)
