package `in`.dragonbra.javasteam.depotdownloader

/**
 * Exception thrown when content download operations fail.
 * Used to indicate errors during depot downloads, manifest retrieval,
 * or other content downloader operations.
 *
 * @author Lossy
 * @since Oct 1, 2025
 */
class DepotDownloaderException(value: String) : Exception(value)
