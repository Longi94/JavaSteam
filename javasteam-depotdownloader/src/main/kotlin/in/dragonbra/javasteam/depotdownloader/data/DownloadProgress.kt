package `in`.dragonbra.javasteam.depotdownloader.data

/**
 * Reports overall download progress across all queued items.
 * Provides high-level statistics for the entire download session, tracking
 * which item is currently processing and cumulative byte transfer.
 *
 * @property currentItem Number of items completed (1-based)
 * @property totalItems Total number of items in the download session
 * @property totalBytesDownloaded Cumulative uncompressed bytes downloaded across all depots
 * @property totalBytesExpected Total uncompressed bytes expected for all items
 * @property status Current download phase
 * @property percentComplete Calculated completion percentage (0.0 to 100.0)
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
 * Reports download progress for a specific depot within an item.
 * Tracks both file-level progress (allocation/validation) and byte-level
 * download progress. During the [DownloadStatus.PREPARING] phase, tracks
 * file allocation; during [DownloadStatus.DOWNLOADING], tracks actual transfers.
 *
 * @property depotId The Steam depot identifier
 * @property filesCompleted Number of files fully allocated or downloaded
 * @property totalFiles Total files to process in this depot (excludes directories)
 * @property bytesDownloaded Uncompressed bytes successfully downloaded
 * @property totalBytes Total uncompressed bytes expected for this depot
 * @property status Current depot processing phase
 * @property percentComplete Calculated completion percentage (0.0 to 100.0)
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
 * Reports download progress for an individual file.
 * Provides chunk-level granularity for tracking file downloads. Updates are
 * throttled to every 500ms to avoid excessive callback overhead.
 *
 * @property depotId The Steam depot containing this file
 * @property fileName Relative path of the file within the depot
 * @property bytesDownloaded Approximate uncompressed bytes downloaded (based on chunk completion)
 * @property totalBytes Total uncompressed file size
 * @property chunksCompleted Number of chunks successfully downloaded and written
 * @property totalChunks Total chunks comprising this file
 * @property status Current file download status
 * @property percentComplete Calculated completion percentage (0.0 to 100.0)
 *
 * @author Lossy
 * @since Oct 1, 2025
 */
data class FileProgress(
    val depotId: Int,
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

/**
 * Represents the current phase of a download operation.
 *
 * @property PREPARING File allocation and validation phase. Files are being pre-allocated on disk and existing content is being verified.
 * @property DOWNLOADING Active chunk download phase. Content is being transferred from CDN.
 * @property COMPLETED Download finished successfully. All files written and verified.
 */
enum class DownloadStatus {
    PREPARING,
    DOWNLOADING,
    COMPLETED,
}
