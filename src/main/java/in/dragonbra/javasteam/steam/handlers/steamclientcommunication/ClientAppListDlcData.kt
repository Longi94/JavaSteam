package `in`.dragonbra.javasteam.steam.handlers.steamclientcommunication

import `in`.dragonbra.javasteam.util.JavaSteamAddition

/**
 * Information about a DLC (Downloadable Content) associated with an application.
 * @param appId The DLC's application ID.
 * @param app The DLC's name.
 * @param installed Installation status (0 = not installed, non-zero = installed).
 */
@JavaSteamAddition
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
