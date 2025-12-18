package `in`.dragonbra.javasteam.steam.handlers.steamcontent

import `in`.dragonbra.javasteam.util.JavaSteamAddition

/**
 * TODO kdoc
 * @param serverPort
 * @param installedDepots
 * @param accessToken
 */
@JavaSteamAddition
data class RequestPeerContentServer(
    val serverPort: Int,
    val installedDepots: List<Int>,
    val accessToken: Long,
) {
    override fun toString(): String = """
            RequestPeerContentServer(
                serverPort=$serverPort,
                installedDepots=$installedDepots,
                accessToken=$accessToken
            )
    """.trimIndent()
}
