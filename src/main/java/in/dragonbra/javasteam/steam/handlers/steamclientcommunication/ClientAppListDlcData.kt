package `in`.dragonbra.javasteam.steam.handlers.steamclientcommunication

/**
 * TODO kdoc
 * @param appId
 * @param app
 * @param installed
 */
data class ClientAppListDlcData(
    val appId: Int,
    val app: String,
    val installed: Int,
) {
    override fun toString(): String = """
           ClientAppListDlcData(
                appId=$appId,
                app='$app',
                installed=$installed
           )
    """.trimIndent()
}
