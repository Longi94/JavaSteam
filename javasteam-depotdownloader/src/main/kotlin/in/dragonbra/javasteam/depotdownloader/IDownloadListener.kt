package `in`.dragonbra.javasteam.depotdownloader

import `in`.dragonbra.javasteam.depotdownloader.data.DepotProgress
import `in`.dragonbra.javasteam.depotdownloader.data.DownloadItem
import `in`.dragonbra.javasteam.depotdownloader.data.FileProgress
import `in`.dragonbra.javasteam.depotdownloader.data.OverallProgress

/**
 * Listener interface for download events.
 *
 * @author Lossy
 * @since Oct 1, 2025
 */
interface IDownloadListener {
    // Queue management
    fun onItemAdded(item: DownloadItem, index: Int) {}
    fun onItemRemoved(item: DownloadItem, index: Int) {}
    fun onQueueCleared(previousItems: List<DownloadItem>) {}

    // Download lifecycle
    fun onDownloadStarted(item: DownloadItem) {}
    fun onDownloadCompleted(item: DownloadItem) {}
    fun onDownloadFailed(item: DownloadItem, error: Throwable) {}

    // Progress tracking
    fun onOverallProgress(progress: OverallProgress) {}
    fun onDepotProgress(depotId: Int, progress: DepotProgress) {}
    fun onFileProgress(depotId: Int, fileName: String, progress: FileProgress) {}

    // Status updates
    fun onStatusUpdate(message: String) {}

    // Configuration
    fun onAndroidEmulation(value: Boolean) {}
}
