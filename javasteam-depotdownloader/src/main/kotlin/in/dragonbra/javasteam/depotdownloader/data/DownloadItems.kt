package `in`.dragonbra.javasteam.depotdownloader.data

// https://kotlinlang.org/docs/coding-conventions.html#source-file-organization

/**
 * Base class for downloadable Steam content items.
 * @property appId The Steam application ID
 * @property installDirectory Optional custom installation directory path
 * @property installToGameNameDirectory If true, installs to a directory named after the game
 * @property downloadManifestOnly If true, only downloads the manifest file without actual content
 *
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
 * Represents a Steam Workshop (UGC - User Generated Content) item for download.
 *
 * @property ugcId The unique UGC item identifier
 *
 * @author Lossy
 * @since Oct 1, 2025
 */
class UgcItem @JvmOverloads constructor(
    appId: Int,
    val ugcId: Long,
    installToGameNameDirectory: Boolean = false,
    installDirectory: String? = null,
    downloadManifestOnly: Boolean = false,
) : DownloadItem(appId, installDirectory, installToGameNameDirectory, downloadManifestOnly)

/**
 * Represents a Steam published file for download.
 *
 * @property pubfile The published file identifier
 *
 * @author Lossy
 * @since Oct 1, 2025
 */
class PubFileItem @JvmOverloads constructor(
    appId: Int,
    val pubfile: Long,
    installToGameNameDirectory: Boolean = false,
    installDirectory: String? = null,
    downloadManifestOnly: Boolean = false,
) : DownloadItem(appId, installDirectory, installToGameNameDirectory, downloadManifestOnly)

/**
 * Represents a Steam application/game for download from a depot.
 *
 * @property branch The branch name to download from (e.g., "public", "beta")
 * @property branchPassword Password for password-protected branches
 * @property downloadAllPlatforms If true, downloads depots for all platforms
 * @property os Operating system filter (e.g., "windows", "macos", "linux")
 * @property downloadAllArchs If true, downloads depots for all architectures
 * @property osArch Architecture filter (e.g., "32", "64")
 * @property downloadAllLanguages If true, downloads depots for all languages
 * @property language Language filter (e.g., "english", "french")
 * @property lowViolence If true, downloads low-violence versions where available
 * @property depot List of specific depot IDs to download
 * @property manifest List of specific manifest IDs corresponding to depot IDs
 *
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
