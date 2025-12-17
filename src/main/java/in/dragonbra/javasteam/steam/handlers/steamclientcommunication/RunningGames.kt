package `in`.dragonbra.javasteam.steam.handlers.steamclientcommunication

/**
 * TODO kdoc
 * @param appId
 * @param extraInfo
 * @param timeRunningSec
 */
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
