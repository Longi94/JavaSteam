package `in`.dragonbra.javasteam.steam.handlers.steamclientcommunication

import `in`.dragonbra.javasteam.util.JavaSteamAddition

/**
 * Detailed information about an application on a Steam client, including its download/update state.
 * @param appid The application ID.
 * @param app The application name.
 * @param category The application category.
 * @param appType The type of application (e.g., game, tool, DLC).
 * @param numDownloading Number of items currently downloading for this app.
 * @param bytesDownloadRate Current download speed in bytes per second.
 * @param bytesDownloaded Total bytes already downloaded.
 * @param bytesToDownload Total bytes that need to be downloaded.
 * @param dlcs List of DLC data associated with this application.
 * @param favorite Whether this app is marked as a favorite.
 * @param autoUpdate Whether automatic updates are enabled for this app.
 * @param installed Whether the app is currently installed.
 * @param downloadPaused Whether the download is currently paused.
 * @param changing Whether the app is currently changing state (installing/updating/uninstalling).
 * @param availableOnPlatform Whether the app is available on the current platform.
 * @param bytesStaged Bytes that have been staged for installation.
 * @param bytesToStage Total bytes that need to be staged.
 * @param bytesRequired Total disk space required for the app.
 * @param sourceBuildId The current build ID installed.
 * @param targetBuildId The build ID being updated to.
 * @param estimatedSecondsRemaining Estimated time remaining for the current operation in seconds.
 * @param queuePosition Position in the download queue (-1 if not queued).
 * @param uninstalling Whether the app is currently being uninstalled.
 * @param rtTimeScheduled Scheduled time for the update/download (Unix timestamp).
 * @param running Whether the app is currently running.
 * @param updatePercentage Progress percentage of the current update/download operation.
 */
@JavaSteamAddition
data class ClientAppListAppData(
    val appid: Int,
    val app: String,
    val category: String,
    val appType: String,
    val numDownloading: Int,
    val bytesDownloadRate: Int,
    val bytesDownloaded: Long,
    val bytesToDownload: Long,
    val dlcs: List<ClientAppListDlcData>,
    val favorite: Boolean,
    val autoUpdate: Boolean,
    val installed: Boolean,
    val downloadPaused: Boolean,
    val changing: Boolean,
    val availableOnPlatform: Boolean,
    val bytesStaged: Long,
    val bytesToStage: Long,
    val bytesRequired: Long,
    val sourceBuildId: Int,
    val targetBuildId: Int,
    val estimatedSecondsRemaining: Int,
    val queuePosition: Int,
    val uninstalling: Boolean,
    val rtTimeScheduled: Int,
    val running: Boolean,
    val updatePercentage: Int,
) {
    override fun toString(): String = """
            ClientAppListAppData(
                appid=$appid,
                app='$app',
                category='$category',
                appType='$appType',
                numDownloading=$numDownloading,
                bytesDownloadRate=$bytesDownloadRate,
                bytesDownloaded=$bytesDownloaded,
                bytesToDownload=$bytesToDownload,
                dlcs=$dlcs,
                favorite=$favorite,
                autoUpdate=$autoUpdate,
                installed=$installed,
                downloadPaused=$downloadPaused,
                changing=$changing,
                availableOnPlatform=$availableOnPlatform,
                bytesStaged=$bytesStaged,
                bytesToStage=$bytesToStage,
                bytesRequired=$bytesRequired,
                sourceBuildId=$sourceBuildId,
                targetBuildId=$targetBuildId,
                estimatedSecondsRemaining=$estimatedSecondsRemaining,
                queuePosition=$queuePosition,
                uninstalling=$uninstalling,
                rtTimeScheduled=$rtTimeScheduled,
                running=$running,
                updatePercentage=$updatePercentage
            )
    """.trimIndent()
}
