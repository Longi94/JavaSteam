package `in`.dragonbra.javasteam.steam.handlers.steamcontent

import `in`.dragonbra.javasteam.util.JavaSteamAddition

/**
 * Represents the response from [SteamContent.getPeerContentInfo].
 * @param appIds list of app IDs for which peer content is available on the remote client.
 * @param ipPublic public IP address of the remote peer content server.
 */
@JavaSteamAddition
data class GetPeerContentInfo(
    val appIds: List<Int>,
    val ipPublic: String,
)
