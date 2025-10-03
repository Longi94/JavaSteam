package `in`.dragonbra.javasteam.depotdownloader.data

/**
 * Overall download progress across all items.
 *
 * @author Lossy
 * @since Oct 1, 2025
 */
data class OverallProgress(
    val currentItem: Int,
    val totalItems: Int,
    val totalBytesDownloaded: Long,
    val totalBytesExpected: Long,
    val status: DownloadStatus,
) {
    val percentComplete: Double
        get() = if (totalBytesExpected > 0) {
            (totalBytesDownloaded.toDouble() / totalBytesExpected) * 100.0
        } else {
            0.0
        }
}

/**
 * Progress for a specific depot
 *
 * @author Lossy
 * @since Oct 1, 2025
 */
data class DepotProgress(
    val depotId: Int,
    val filesCompleted: Int,
    val totalFiles: Int,
    val bytesDownloaded: Long,
    val totalBytes: Long,
    val status: DownloadStatus,
) {
    val percentComplete: Double
        get() = if (totalBytes > 0) {
            (bytesDownloaded.toDouble() / totalBytes) * 100.0
        } else {
            0.0
        }
}

/**
 * Progress for a specific file
 *
 * @author Lossy
 * @since Oct 1, 2025
 */
data class FileProgress(
    val fileName: String,
    val bytesDownloaded: Long,
    val totalBytes: Long,
    val chunksCompleted: Int,
    val totalChunks: Int,
    val status: DownloadStatus,
) {
    val percentComplete: Double
        get() = if (totalBytes > 0) {
            (bytesDownloaded.toDouble() / totalBytes) * 100.0
        } else {
            0.0
        }
}

enum class DownloadStatus {
    PREPARING,
    DOWNLOADING,
    COMPLETED,
}
