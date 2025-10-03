package `in`.dragonbra.javasteam.depotdownloader.data

// https://kotlinlang.org/docs/coding-conventions.html#source-file-organization

/**
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
