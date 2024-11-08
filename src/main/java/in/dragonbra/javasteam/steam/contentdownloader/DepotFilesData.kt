package `in`.dragonbra.javasteam.steam.contentdownloader

import `in`.dragonbra.javasteam.types.DepotManifest

data class DepotFilesData(
    val depotDownloadInfo: DepotDownloadInfo,
    val depotCounter: DepotDownloadCounter,
    val stagingDir: String,
    val manifest: DepotManifest,
    val previousManifest: DepotManifest?,
)
