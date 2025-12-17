package `in`.dragonbra.javasteam.steam.handlers.steamclientcommunication

import `in`.dragonbra.javasteam.util.JavaSteamAddition

/**
 * Information about a game currently running on a Steam client.
 * @param appId The application ID of the running game.
 * @param extraInfo Additional information about the running game.
 * @param timeRunningSec Duration in seconds that the game has been running.
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
