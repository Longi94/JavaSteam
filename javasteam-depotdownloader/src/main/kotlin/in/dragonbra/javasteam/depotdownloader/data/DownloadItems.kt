package `in`.dragonbra.javasteam.depotdownloader.data

// https://kotlinlang.org/docs/coding-conventions.html#source-file-organization

/**
 * Base class for downloadable Steam content items.
 * @property appId The Steam application ID
 * @property installDirectory Optional custom installation directory path. If null, defaults to a
 * depot-specific subdirectory under the current working directory.
 * @property installToGameNameDirectory If true, installs into a subdirectory named after the game
 * instead of directly into [installDirectory].
 * @property verify If true, validates existing files against the manifest before downloading.
 * @property downloadManifestOnly If true, saves the depot manifest to disk without downloading content.
 *
 * @author Lossy
 * @since Oct 1, 2025
 */
abstract class DownloadItem(
    val appId: Int,
    val installDirectory: String?,
    val installToGameNameDirectory: Boolean,
    val verify: Boolean,
    val downloadManifestOnly: Boolean,
)

/**
 * Represents a Steam Workshop (UGC - User Generated Content) item for download.
 * Prefer [Builder] when calling from Java. From Kotlin, use the primary constructor with named arguments.
 * @property ugcId The unique UGC handle identifying the workshop item.
 * **Kotlin:**
 * ```kotlin
 * val item = UgcItem(appId = 440, ugcId = 123456789L, installDirectory = "tf2")
 * ```
 * **Java:**
 * ```java
 * var item = new UgcItem.Builder(440, 123456789L)
 *     .installDirectory("tf2")
 *     .build();
 * ```
 * @author Lossy
 * @since Oct 1, 2025
 */
class UgcItem @JvmOverloads constructor(
    appId: Int,
    val ugcId: Long,
    installToGameNameDirectory: Boolean = false,
    installDirectory: String? = null,
    verify: Boolean = false,
    downloadManifestOnly: Boolean = false,
) : DownloadItem(appId, installDirectory, installToGameNameDirectory, verify, downloadManifestOnly) {

    @Suppress("unused")
    class Builder(private val appId: Int, private val ugcId: Long) {
        private var installToGameNameDirectory: Boolean = false
        private var installDirectory: String? = null
        private var verify: Boolean = false
        private var downloadManifestOnly: Boolean = false

        fun installToGameNameDirectory(v: Boolean) = apply { installToGameNameDirectory = v }
        fun installDirectory(v: String?) = apply { installDirectory = v }
        fun verify(v: Boolean) = apply { verify = v }
        fun downloadManifestOnly(v: Boolean) = apply { downloadManifestOnly = v }

        fun build() = UgcItem(
            appId = appId,
            ugcId = ugcId,
            installToGameNameDirectory = installToGameNameDirectory,
            installDirectory = installDirectory,
            verify = verify,
            downloadManifestOnly = downloadManifestOnly,
        )
    }
}

/**
 * Represents a Steam published file for download.
 *
 * Prefer [Builder] when calling from Java. From Kotlin, use the primary constructor with named arguments.
 * @property pubFile The published file ID identifying the item on the Steam Workshop.
 * **Kotlin:**
 * ```kotlin
 * val item = PubFileItem(appId = 440, pubFile = 123456789L, installDirectory = "tf2")
 * ```
 * **Java:**
 * ```java
 * var item = new PubFileItem.Builder(440, 123456789L)
 *     .installDirectory("tf2")
 *     .build();
 * ```
 * @author Lossy
 * @since Oct 1, 2025
 */
class PubFileItem @JvmOverloads constructor(
    appId: Int,
    val pubFile: Long,
    installToGameNameDirectory: Boolean = false,
    installDirectory: String? = null,
    verify: Boolean = false,
    downloadManifestOnly: Boolean = false,
) : DownloadItem(appId, installDirectory, installToGameNameDirectory, verify, downloadManifestOnly) {

    @Suppress("unused")
    class Builder(private val appId: Int, private val pubFile: Long) {
        private var installToGameNameDirectory: Boolean = false
        private var installDirectory: String? = null
        private var verify: Boolean = false
        private var downloadManifestOnly: Boolean = false

        fun installToGameNameDirectory(v: Boolean) = apply { installToGameNameDirectory = v }
        fun installDirectory(v: String?) = apply { installDirectory = v }
        fun verify(v: Boolean) = apply { verify = v }
        fun downloadManifestOnly(v: Boolean) = apply { downloadManifestOnly = v }

        fun build() = PubFileItem(
            appId = appId,
            pubFile = pubFile,
            installToGameNameDirectory = installToGameNameDirectory,
            installDirectory = installDirectory,
            verify = verify,
            downloadManifestOnly = downloadManifestOnly,
        )
    }
}

/**
 * Represents a Steam application or game for download from a depot.
 * Prefer [Builder] when calling from Java. From Kotlin, use the primary constructor with named arguments.
 * @property branch Branch to download from (e.g. `"public"`, `"beta"`). Defaults to `"public"` when null.
 * @property branchPassword Password for password-protected branches.
 * @property downloadAllPlatforms If true, ignores the [os] filter and downloads depots for all platforms.
 * @property os Operating system filter (e.g. `"windows"`, `"macos"`, `"linux"`). Null means use the
 * host OS.
 * @property downloadAllArchs If true, ignores the [osArch] filter and downloads depots for all architectures.
 * @property osArch Architecture filter (e.g. `"32"`, `"64"`). Null means use the host architecture.
 * @property downloadAllLanguages If true, ignores the [language] filter and downloads depots for all languages.
 * @property language Language filter (e.g. `"english"`, `"french"`). Null means use `"english"`.
 * @property lowViolence If true, prefers low-violence depot variants where available.
 * @property depot Specific depot IDs to download. Empty list means download all depots for the app.
 * @property manifest Specific manifest IDs paired 1:1 with [depot]. Empty list means use the latest manifest.
 * **Kotlin:**
 * ```kotlin
 * val item = AppItem(
 *     appId = 1303350,
 *     installDirectory = "steamapps",
 *     branch = "public",
 *     os = "windows",
 *     osArch = "64",
 *     language = "english",
 * )
 * ```
 *
 * **Java:**
 * ```java
 * var item = new AppItem.Builder(1303350)
 *     .installDirectory("steamapps")
 *     .branch("public")
 *     .os("windows")
 *     .osArch("64")
 *     .language("english")
 *     .build();
 * ```
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
    verify: Boolean = false,
    downloadManifestOnly: Boolean = false,
) : DownloadItem(appId, installDirectory, installToGameNameDirectory, verify, downloadManifestOnly) {

    @Suppress("unused")
    class Builder(private val appId: Int) {
        private var installToGameNameDirectory: Boolean = false
        private var installDirectory: String? = null
        private var branch: String? = null
        private var branchPassword: String? = null
        private var downloadAllPlatforms: Boolean = false
        private var os: String? = null
        private var downloadAllArchs: Boolean = false
        private var osArch: String? = null
        private var downloadAllLanguages: Boolean = false
        private var language: String? = null
        private var lowViolence: Boolean = false
        private var depot: List<Int> = emptyList()
        private var manifest: List<Long> = emptyList()
        private var verify: Boolean = false
        private var downloadManifestOnly: Boolean = false

        fun installToGameNameDirectory(v: Boolean) = apply { installToGameNameDirectory = v }
        fun installDirectory(v: String?) = apply { installDirectory = v }
        fun branch(v: String?) = apply { branch = v }
        fun branchPassword(v: String?) = apply { branchPassword = v }
        fun downloadAllPlatforms(v: Boolean) = apply { downloadAllPlatforms = v }
        fun os(v: String?) = apply { os = v }
        fun downloadAllArchs(v: Boolean) = apply { downloadAllArchs = v }
        fun osArch(v: String?) = apply { osArch = v }
        fun downloadAllLanguages(v: Boolean) = apply { downloadAllLanguages = v }
        fun language(v: String?) = apply { language = v }
        fun lowViolence(v: Boolean) = apply { lowViolence = v }
        fun depot(v: List<Int>) = apply { depot = v }
        fun manifest(v: List<Long>) = apply { manifest = v }
        fun verify(v: Boolean) = apply { verify = v }
        fun downloadManifestOnly(v: Boolean) = apply { downloadManifestOnly = v }

        fun build() = AppItem(
            appId = appId,
            installToGameNameDirectory = installToGameNameDirectory,
            installDirectory = installDirectory,
            branch = branch,
            branchPassword = branchPassword,
            downloadAllPlatforms = downloadAllPlatforms,
            os = os,
            downloadAllArchs = downloadAllArchs,
            osArch = osArch,
            downloadAllLanguages = downloadAllLanguages,
            language = language,
            lowViolence = lowViolence,
            depot = depot,
            manifest = manifest,
            verify = verify,
            downloadManifestOnly = downloadManifestOnly,
        )
    }
}
