package `in`.dragonbra.javasteam.depotdownloader.data

import okio.Path

/**
 * Contains all information required to download a specific depot manifest and its content.
 * This class aggregates the depot identification, authentication, and installation details
 * needed to perform a complete depot download operation. It is created during the depot
 * resolution phase and passed through the download pipeline.
 *
 * @property depotId The Steam depot identifier
 * @property appId The owning application ID (may differ from the app being downloaded if the depot uses `depotfromapp` proxying)
 * @property manifestId The specific manifest version to download
 * @property branch The branch name this manifest belongs to (e.g., "public", "beta")
 * @property installDir The target directory for downloaded files
 * @property depotKey The AES decryption key for depot chunks. Cleared on disposal for security.
 *
 * @author Oxters
 * @author Lossy
 * @since Oct 29, 2024
 */
data class DepotDownloadInfo(
    val depotId: Int,
    val appId: Int,
    val manifestId: Long,
    val branch: String,
    val installDir: Path,
    val depotKey: ByteArray,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is DepotDownloadInfo) return false

        if (depotId != other.depotId) return false
        if (appId != other.appId) return false
        if (manifestId != other.manifestId) return false
        if (branch != other.branch) return false
        if (installDir != other.installDir) return false
        if (!depotKey.contentEquals(other.depotKey)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = depotId
        result = 31 * result + appId
        result = 31 * result + manifestId.hashCode()
        result = 31 * result + branch.hashCode()
        result = 31 * result + installDir.hashCode()
        result = 31 * result + depotKey.contentHashCode()
        return result
    }
}
