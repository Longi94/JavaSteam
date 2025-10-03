package `in`.dragonbra.javasteam.depotdownloader.data

import `in`.dragonbra.javasteam.depotdownloader.ContentDownloader

// https://kotlinlang.org/docs/coding-conventions.html#source-file-organization

/**
 * @author Lossy
 * @since Oct 1, 2025
 */
abstract class DownloadItem(
    val appId: Int,
    val installDirectory: String?,
    val installToGameNameDirectory: Boolean,
    val downloadManifestOnly: Boolean,
)

/**
 * @author Lossy
 * @since Oct 1, 2025
 */
class UgcItem @JvmOverloads constructor(
    appId: Int,
    val ugcId: Long = ContentDownloader.INVALID_MANIFEST_ID,
    installToGameNameDirectory: Boolean = false,
    installDirectory: String? = null,
    downloadManifestOnly: Boolean = false,
) : DownloadItem(appId, installDirectory, installToGameNameDirectory, downloadManifestOnly)

/**
 * @author Lossy
 * @since Oct 1, 2025
 */
class PubFileItem @JvmOverloads constructor(
    appId: Int,
    val pubfile: Long = ContentDownloader.INVALID_MANIFEST_ID,
    installToGameNameDirectory: Boolean = false,
    installDirectory: String? = null,
    downloadManifestOnly: Boolean = false,
) : DownloadItem(appId, installDirectory, installToGameNameDirectory, downloadManifestOnly)

/**
 * @author Lossy
 * @since Oct 1, 2025
 */
class AppItem @JvmOverloads constructor(
    appId: Int,
    installToGameNameDirectory: Boolean = false,
    installDirectory: String? = null,
    val branch: String? = null,
    val branchPassword: String? = null,
    val downloadAllPlatforms: Boolean = false,
    val os: String? = null,
    val downloadAllArchs: Boolean = false,
    val osArch: String? = null,
    val downloadAllLanguages: Boolean = false,
    val language: String? = null,
    val lowViolence: Boolean = false,
    val depot: List<Int> = emptyList(),
    val manifest: List<Long> = emptyList(),
    downloadManifestOnly: Boolean = false,
) : DownloadItem(appId, installDirectory, installToGameNameDirectory, downloadManifestOnly)
