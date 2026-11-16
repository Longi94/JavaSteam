package `in`.dragonbra.javasteam.depotdownloader

import `in`.dragonbra.javasteam.depotdownloader.data.DownloadItem

/**
 * Listener interface for receiving download progress and status events.
 *
 * All methods have default empty implementations, allowing listeners to
 * implement only the callbacks they need.
 *
 * @author Lossy
 * @since Oct 1, 2025
 */
interface IDownloadListener {
    /**
     * Called when an item is added to the download queue.
     */
    fun onItemAdded(item: DownloadItem) {}

    /**
     * Called when a download begins processing.
     */
    fun onDownloadStarted(item: DownloadItem) {}

    /**
     * Called when a download completes successfully.
     */
    fun onDownloadCompleted(item: DownloadItem) {}

    /**
     * Called when a download fails with an error.
     */
    fun onDownloadFailed(item: DownloadItem, error: Throwable) {}

    /**
     * Called when a depot that was part of a download is left out, for example because the account
     * has no access to it, no depot key could be obtained, or it has no manifest for the branch.
     * The rest of the download continues; if every depot is skipped, [onDownloadFailed] is called.
     *
     * @param depotId The depot that will not be downloaded
     * @param reason An explanation
     */
    fun onDepotSkipped(depotId: Int, reason: String) {}

    /**
     * Called during file preparation with informational messages.
     * Examples: "Pre-allocating depots\441\file.txt", "Validating file.cab"
     */
    fun onStatusUpdate(message: String) {}

    /**
     * Called when a file completes downloading, after the [onChunkCompleted] of its last chunk.
     * Use this for printing progress like "20.42% depots\441\maps\ctf_haarp.bsp"
     *
     * @param depotId The depot being downloaded
     * @param fileName Relative file path
     * @param depotPercentComplete Overall depot completion percentage (0f to 1f)
     */
    fun onFileCompleted(depotId: Int, fileName: String, depotPercentComplete: Float) {}

    /**
     * Called when a chunk completes downloading, for every chunk including the last one of a file.
     * Provides more frequent progress updates than onFileCompleted, and is the one to count bytes in.
     *
     * @param depotId The depot being downloaded
     * @param depotPercentComplete Overall depot completion percentage (0f to 1f)
     * @param compressedBytes Total compressed bytes downloaded so far for this depot
     * @param uncompressedBytes Total uncompressed bytes downloaded so far for this depot
     */
    fun onChunkCompleted(depotId: Int, depotPercentComplete: Float, compressedBytes: Long, uncompressedBytes: Long) {}

    /**
     * Called when a depot finishes downloading.
     * Use this for printing summary like "Depot 228990 - Downloaded X bytes (Y bytes uncompressed)"
     *
     * @param depotId The depot that completed
     * @param compressedBytes Bytes transferred (compressed)
     * @param uncompressedBytes Actual data size (uncompressed)
     */
    fun onDepotCompleted(depotId: Int, compressedBytes: Long, uncompressedBytes: Long) {}
}
