package `in`.dragonbra.javasteam.depotdownloader

import `in`.dragonbra.javasteam.depotdownloader.data.DepotProgress
import `in`.dragonbra.javasteam.depotdownloader.data.FileProgress
import `in`.dragonbra.javasteam.depotdownloader.data.OverallProgress

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
     *
     * @param appId The application ID that was queued
     */
    fun onItemAdded(appId: Int) {}

    /**
     * Called when a download begins processing.
     *
     * @param appId The application ID being downloaded
     */
    fun onDownloadStarted(appId: Int) {}

    /**
     * Called when a download completes successfully.
     *
     * @param appId The application ID that finished downloading
     */
    fun onDownloadCompleted(appId: Int) {}

    /**
     * Called when a download fails with an error.
     *
     * @param appId The application ID that failed
     * @param error The exception that caused the failure
     */
    fun onDownloadFailed(appId: Int, error: Throwable) {}

    /**
     * Called periodically with overall download progress across all items.
     * Reports progress for the entire download queue, including completed
     * and remaining items.
     *
     * @param progress Overall download statistics
     */
    fun onOverallProgress(progress: OverallProgress) {}

    /**
     * Called periodically with progress for a specific depot.
     * Reports file allocation and download progress for an individual depot.
     *
     * @param progress Depot-specific download statistics
     */
    fun onDepotProgress(progress: DepotProgress) {}

    /**
     * Called periodically with progress for a specific file.
     * Reports chunk-level download progress for individual files.
     *
     * @param progress File-specific download statistics
     */
    fun onFileProgress(progress: FileProgress) {}

    /**
     * Called with informational status messages during download operations.
     * Used for logging or displaying current operations like manifest
     * downloads, file validation, and allocation.
     *
     * @param message Human-readable status message
     */
    fun onStatusUpdate(message: String) {}
}
