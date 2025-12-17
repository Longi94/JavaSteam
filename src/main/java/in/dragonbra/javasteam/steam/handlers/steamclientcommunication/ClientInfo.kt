package `in`.dragonbra.javasteam.steam.handlers.steamclientcommunication

import `in`.dragonbra.javasteam.util.JavaSteamAddition

/**
 * TODO kdoc
 * @param packageVersion
 * @param os
 * @param machineName
 * @param ipPublic
 * @param ipPrivate
 * @param bytesAvailable
 * @param runningGames
 * @param protocolVersion
 * @param clientCommVersion
 * @param localUsers
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
