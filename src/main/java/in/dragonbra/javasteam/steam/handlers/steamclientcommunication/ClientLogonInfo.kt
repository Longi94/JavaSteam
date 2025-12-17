package `in`.dragonbra.javasteam.steam.handlers.steamclientcommunication

/**
 * TODO kdoc
 * @param sessions
 * @param refetchIntervalSec
 */
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
