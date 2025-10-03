package `in`.dragonbra.javasteam.depotdownloader.data

import okio.Path

/**
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
