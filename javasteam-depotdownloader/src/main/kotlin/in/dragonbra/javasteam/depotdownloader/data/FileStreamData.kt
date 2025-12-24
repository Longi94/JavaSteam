package `in`.dragonbra.javasteam.depotdownloader.data

import kotlinx.coroutines.sync.Mutex
import okio.FileHandle
import java.util.concurrent.atomic.AtomicInteger

/**
 * Internal state for managing concurrent chunk writes to a single file.
 * Coordinates writes from multiple download workers to ensure thread-safe file access
 * and tracks when all chunks have been written.
 *
 * @property fileHandle Shared file handle for all chunk writes. Lazily opened on first write.
 * @property fileLock Mutex protecting concurrent access to [fileHandle]
 * @property chunksToDownload Atomic counter of remaining chunks. File is closed when this reaches zero.
 *
 * @author Oxters
 * @author Lossy
 * @since Oct 29, 2024
 */
data class FileStreamData(
    var fileHandle: FileHandle?,
    val fileLock: Mutex = Mutex(),
    var chunksToDownload: AtomicInteger = AtomicInteger(0),
)
