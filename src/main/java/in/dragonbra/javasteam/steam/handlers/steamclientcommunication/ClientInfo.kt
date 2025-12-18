package `in`.dragonbra.javasteam.steam.handlers.steamclientcommunication

import `in`.dragonbra.javasteam.util.JavaSteamAddition

/**
 * Information about a Steam client machine and its current state.
 * @param packageVersion The Steam client package version.
 * @param os The operating system of the client.
 * @param machineName The name of the client machine.
 * @param ipPublic The client's public IP address.
 * @param ipPrivate The client's private/local IP address.
 * @param bytesAvailable Available disk space in bytes.
 * @param runningGames List of games currently running on the client.
 * @param protocolVersion The protocol version being used.
 * @param clientCommVersion The client communication protocol version.
 * @param localUsers List of local user IDs logged into the client.
 */
@JavaSteamAddition
data class ClientInfo(
    val packageVersion: Int,
    val os: String,
    val machineName: String,
    val ipPublic: String,
    val ipPrivate: String,
    val bytesAvailable: Long,
    val runningGames: List<RunningGames>,
    val protocolVersion: Int,
    val clientCommVersion: Int,
    val localUsers: List<Int>,
) {
    override fun toString(): String = """
            ClientInfo(
                packageVersion=$packageVersion,
                os='$os',
                machineName='$machineName',
                ipPublic='$ipPublic',
                ipPrivate='$ipPrivate',
                bytesAvailable=$bytesAvailable,
                runningGames=$runningGames,
                protocolVersion=$protocolVersion,
                clientCommVersion=$clientCommVersion,
                localUsers=$localUsers
            )
    """.trimIndent()
}
