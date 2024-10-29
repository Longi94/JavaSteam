package `in`.dragonbra.javasteam.steam.contentdownloader

data class DepotDownloadInfo(
    val depotId: Int,
    val appId: Int,
    val manifestId: Long,
    val branch: String,
    val installDir: String,
    val depotKey: ByteArray?,
)
