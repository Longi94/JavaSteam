package `in`.dragonbra.javasteam.steam.handlers.steamcontent

import `in`.dragonbra.javasteam.util.JavaSteamAddition

/**
 * TODO kdoc
 * @param appIds
 * @param ipPublic
 */
@JavaSteamAddition
data class GetPeerContentInfo(
    val appIds: List<Int>,
    val ipPublic: String,
)
