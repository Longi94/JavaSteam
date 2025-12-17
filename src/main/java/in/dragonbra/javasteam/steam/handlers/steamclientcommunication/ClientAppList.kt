package `in`.dragonbra.javasteam.steam.handlers.steamclientcommunication

import `in`.dragonbra.javasteam.util.JavaSteamAddition

/**
 * TODO kdoc
 * @param bytesAvailable
 * @param apps
 * @param clientInfo
 * @param refetchIntervalSecFull
 * @param refetchIntervalSecChanging
 * @param refetchIntervalSecUpdating
 */
@JavaSteamAddition
data class ClientAppList(
    val bytesAvailable: Long,
    val apps: List<ClientAppListAppData>,
    val clientInfo: ClientInfo,
    val refetchIntervalSecFull: Int,
    val refetchIntervalSecChanging: Int,
    val refetchIntervalSecUpdating: Int,
) {
    override fun toString(): String = """
            ClientAppList(
                bytesAvailable=$bytesAvailable,
                apps=$apps,
                clientInfo=$clientInfo,
                refetchIntervalSecFull=$refetchIntervalSecFull,
                refetchIntervalSecChanging=$refetchIntervalSecChanging,
                refetchIntervalSecUpdating=$refetchIntervalSecUpdating
            )
    """.trimIndent()
}
