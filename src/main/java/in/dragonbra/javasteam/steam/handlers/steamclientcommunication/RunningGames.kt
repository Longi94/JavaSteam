package `in`.dragonbra.javasteam.steam.handlers.steamclientcommunication

import `in`.dragonbra.javasteam.util.JavaSteamAddition

/**
 * TODO kdoc
 * @param appId
 * @param extraInfo
 * @param timeRunningSec
 */
@JavaSteamAddition
data class RunningGames(
    val appId: Int,
    val extraInfo: String,
    val timeRunningSec: Int,
) {
    override fun toString(): String = """
            RunningGames(
                appId=$appId,
                extraInfo='$extraInfo',
                timeRunningSec=$timeRunningSec
            )
    """.trimIndent()
}
