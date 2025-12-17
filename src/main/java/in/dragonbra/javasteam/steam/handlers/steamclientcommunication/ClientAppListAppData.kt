package `in`.dragonbra.javasteam.steam.handlers.steamclientcommunication

import `in`.dragonbra.javasteam.util.JavaSteamAddition

/**
 * TODO kdoc
 * @param appid
 * @param app
 * @param category
 * @param appType
 * @param numDownloading
 * @param bytesDownloadRate
 * @param bytesDownloaded
 * @param bytesToDownload
 * @param dlcs
 * @param favorite
 * @param autoUpdate
 * @param installed
 * @param downloadPaused
 * @param changing
 * @param availableOnPlatform
 * @param bytesStaged
 * @param bytesToStage
 * @param bytesRequired
 * @param sourceBuildId
 * @param targetBuildId
 * @param estimatedSecondsRemaining
 * @param queuePosition
 * @param uninstalling
 * @param rtTimeScheduled
 * @param running
 * @param updatePercentage
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
                app='$app', c
                ategory='$category',
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
