package `in`.dragonbra.javasteam.steam.handlers.steamcontent

/**
 * TODO kdoc
 * @param hostsKv
 * @param validUntilTime
 * @param ipCountry
 */
data class ClientUpdateHosts(
    val hostsKv: String,
    val validUntilTime: Long,
    val ipCountry: String,
)
