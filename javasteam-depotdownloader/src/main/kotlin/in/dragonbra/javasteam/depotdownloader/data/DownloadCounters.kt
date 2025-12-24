package `in`.dragonbra.javasteam.depotdownloader.data

// https://kotlinlang.org/docs/coding-conventions.html#source-file-organization

/**
 * Tracks cumulative download statistics across all depots in a download session.
 * Used for overall progress reporting and final download summary. All fields are
 * accessed under synchronization to ensure thread-safe updates from concurrent downloads.
 *
 * @property completeDownloadSize Total bytes expected to download across all depots. Adjusted during validation when existing chunks are reused.
 * @property totalBytesCompressed Total compressed bytes transferred from CDN servers
 * @property totalBytesUncompressed Total uncompressed bytes written to disk
 *
 * @author Oxters
 * @author Lossy
 * @since Oct 29, 2024
 */
data class GlobalDownloadCounter(
    var completeDownloadSize: Long = 0,
    var totalBytesCompressed: Long = 0,
    var totalBytesUncompressed: Long = 0,
)

/**
 * Tracks download statistics for a single depot.
 * Used for depot-level progress reporting. All fields are accessed under synchronization
 * to ensure thread-safe updates from concurrent chunk downloads.
 *
 * @property completeDownloadSize Total bytes expected to download for this depot. Calculated during file allocation phase.
 * @property sizeDownloaded Bytes successfully downloaded and written so far
 * @property depotBytesCompressed Compressed bytes transferred from CDN for this depot
 * @property depotBytesUncompressed Uncompressed bytes written to disk for this depot
 *
 * @author Oxters
 * @author Lossy
 * @since Oct 29, 2024
 */
data class DepotDownloadCounter(
    var completeDownloadSize: Long = 0,
    var sizeDownloaded: Long = 0,
    var depotBytesCompressed: Long = 0,
    var depotBytesUncompressed: Long = 0,
)
