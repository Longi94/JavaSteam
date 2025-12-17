package `in`.dragonbra.javasteam.steam.handlers.steamcontent

import `in`.dragonbra.javasteam.util.JavaSteamAddition

/**
 * TODO kdoc
 * @param hostsKv
 * @param validUntilTime
 * @param ipCountry
 */
@JavaSteamAddition
data class ClientUpdateHosts(
    val hostsKv: String,
    val validUntilTime: Long,
    val ipCountry: String,
)
