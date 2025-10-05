package `in`.dragonbra.javasteam.depotdownloader.data

import `in`.dragonbra.javasteam.types.DepotManifest
import `in`.dragonbra.javasteam.types.FileData
import okio.Path

/**
 * Aggregates all data needed to process and download files for a single depot.
 * Created during manifest processing and passed to the download phase. Contains both
 * the current manifest and optional previous manifest to enable differential updates.
 *
 * @property depotDownloadInfo Core depot identification and authentication details
 * @property depotCounter Progress tracking counters for this depot's download
 * @property stagingDir Temporary directory for in-progress file writes
 * @property manifest The current depot manifest being downloaded
 * @property previousManifest The previously installed manifest, if any. Used to identify reusable chunks and deleted files.
 * @property filteredFiles Files to download after applying platform, language, and user filters. Modified during processing to remove duplicates across depots.
 * @property allFileNames Complete set of filenames in this depot, including directories. Used for cross-depot deduplication and cleanup of deleted files.
 *
 * @author Oxters
 * @author Lossy
 * @since Oct 29, 2024
 */
data class DepotFilesData(
    val depotDownloadInfo: DepotDownloadInfo,
    val depotCounter: DepotDownloadCounter,
    val stagingDir: Path,
    val manifest: DepotManifest,
    val previousManifest: DepotManifest?,
    val filteredFiles: MutableList<FileData>,
    val allFileNames: HashSet<String>,
)
