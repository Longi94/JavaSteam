package `in`.dragonbra.javasteam.steam.handlers.steamcontent

import `in`.dragonbra.javasteam.util.JavaSteamAddition

/**
 * Represents the response from [SteamContent.requestPeerContentServer].
 * @param serverPort The port the peer content server is listening on.
 * @param installedDepots List of depot IDs installed on the remote client and available for transfer.
 * @param accessToken Token used to authenticate with the peer content server.
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
