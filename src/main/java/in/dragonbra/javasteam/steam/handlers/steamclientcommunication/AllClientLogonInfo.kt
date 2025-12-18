package `in`.dragonbra.javasteam.steam.handlers.steamclientcommunication

import `in`.dragonbra.javasteam.util.JavaSteamAddition

/**
 * Information about all active Steam clients on the network.
 * @param sessions A list of active client sessions with their logon information.
 * @param refetchIntervalSec The recommended interval in seconds to refetch this data.
 */
@JavaSteamAddition
data class AllClientLogonInfo(
    val sessions: List<AllClientLogonInfoSession>,
    val refetchIntervalSec: Int,
) {
    override fun toString(): String = """
           AllClientLogonInfo(
                sessions=$sessions,
                refetchIntervalSec=$refetchIntervalSec
           )
    """.trimIndent()
}
