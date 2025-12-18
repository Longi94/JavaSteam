package `in`.dragonbra.javasteam.steam.handlers.steamclientcommunication

import `in`.dragonbra.javasteam.util.JavaSteamAddition

/**
 * Response containing the list of applications on a Steam client and related metadata.
 * @param bytesAvailable Total bytes of storage available on the client.
 * @param apps List of application data for each app on the client.
 * @param clientInfo Information about the client machine and state.
 * @param refetchIntervalSecFull Recommended interval in seconds to refetch the complete app list.
 * @param refetchIntervalSecChanging Recommended interval in seconds to refetch when apps are changing state.
 * @param refetchIntervalSecUpdating Recommended interval in seconds to refetch when apps are updating.
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
