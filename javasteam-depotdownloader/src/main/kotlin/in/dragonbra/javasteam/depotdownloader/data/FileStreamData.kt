package `in`.dragonbra.javasteam.depotdownloader.data

import kotlinx.coroutines.sync.Mutex
import okio.FileHandle
import java.util.concurrent.atomic.AtomicInteger

/**
 * @author Oxters
 * @author Lossy
 * @since Oct 29, 2024
 */
data class FileStreamData(
    var fileHandle: FileHandle?,
    val fileLock: Mutex = Mutex(),
    var chunksToDownload: AtomicInteger = AtomicInteger(0),
)
