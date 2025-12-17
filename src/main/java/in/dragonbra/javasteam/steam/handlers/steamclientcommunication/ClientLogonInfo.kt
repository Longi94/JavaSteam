package `in`.dragonbra.javasteam.steam.handlers.steamclientcommunication

import `in`.dragonbra.javasteam.util.JavaSteamAddition

/**
 * TODO kdoc
 * @param sessions
 * @param refetchIntervalSec
 */
@JavaSteamAddition
data class ClientLogonInfo(
    val sessions: List<ClientLogonInfoSession>,
    val refetchIntervalSec: Int,
) {
    override fun toString(): String = """
           ClientLogonInfo(
            sessions=$sessions,
            refetchIntervalSec=$refetchIntervalSec
           )
    """.trimIndent()
}
