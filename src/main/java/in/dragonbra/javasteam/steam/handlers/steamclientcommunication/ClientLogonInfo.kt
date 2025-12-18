package `in`.dragonbra.javasteam.steam.handlers.steamclientcommunication

/**
 * Logon information for a specific Steam client.
 * @param protocolVersion Protocol version of the client.
 * @param os Operating system name.
 * @param machineName Name of the client machine.
 */
data class ClientLogonInfo(
    val protocolVersion: Int,
    val os: String,
    val machineName: String,
)
