package `in`.dragonbra.javasteam.depotdownloader

import `in`.dragonbra.javasteam.depotdownloader.data.*
import `in`.dragonbra.javasteam.enums.EAccountType
import `in`.dragonbra.javasteam.enums.EAppInfoSection
import `in`.dragonbra.javasteam.enums.EDepotFileFlag
import `in`.dragonbra.javasteam.enums.EWorkshopFileType
import `in`.dragonbra.javasteam.steam.cdn.ClientLancache
import `in`.dragonbra.javasteam.steam.cdn.DepotChunk
import `in`.dragonbra.javasteam.steam.cdn.Server
import `in`.dragonbra.javasteam.steam.handlers.steamapps.License
import `in`.dragonbra.javasteam.steam.handlers.steamapps.callback.LicenseListCallback
import `in`.dragonbra.javasteam.steam.handlers.steamcloud.callback.UGCDetailsCallback
import `in`.dragonbra.javasteam.steam.steamclient.SteamClient
import `in`.dragonbra.javasteam.types.*
import `in`.dragonbra.javasteam.util.Adler32
import `in`.dragonbra.javasteam.util.SteamKitWebRequestException
import `in`.dragonbra.javasteam.util.Strings
import `in`.dragonbra.javasteam.util.log.LogManager
import `in`.dragonbra.javasteam.util.log.Logger
import kotlinx.coroutines.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.selects.select
import kotlinx.coroutines.sync.*
import okhttp3.OkHttpClient
import okio.FileSystem
import okio.Path
import okio.Path.Companion.toPath
import org.apache.commons.lang3.SystemUtils
import java.io.Closeable
import java.io.IOException
import java.io.RandomAccessFile
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.concurrent.*
import java.util.concurrent.atomic.*

/**
 * Downloads games, workshop items, and other Steam content via depot manifests.
 *
 * This class provides a queue-based FIFO download system that processes items sequentially.
 * Items are added via [add] or [addAll] and processed automatically in order. The processing
 * loop starts immediately upon construction and waits for items to be queued.
 *
 * ## Download Process
 * 1. Validates account access and obtains depot keys
 * 2. Downloads and caches depot manifests
 * 3. Allocates disk space for files
 * 4. Downloads chunks concurrently (configured by [maxDownloads])
 * 5. Verifies checksums and moves files to final location
 *
 * ## Thread Safety
 * Methods [add], [addAll], [addListener], and [removeListener] are thread-safe.
 * Multiple concurrent calls are supported.
 *
 * @param steamClient Connected Steam client instance with valid login session
 * @param licenses User's license list from [LicenseListCallback]. Required to determine which depots the account has access to.
 * @param debug Enables detailed logging of all operations via [LogManager]
 * @param useLanCache Attempts to detect and use local Steam cache servers (e.g., LANCache) for faster downloads on local networks
 * @param maxDownloads Number of concurrent chunk downloads. Automatically increased to 25 when a LAN cache is detected. Default: 8.
 * Higher is faster on a good connection, lower is easier on memory, the network and the CDN servers. All of them may go to
 * one server, which is what a LAN cache needs.
 * @param maxFileWrites Number of concurrent files being written. Default: 1
 * @param androidEmulation Forces "Windows" as the default OS filter. Used when running Android games in PC emulators that expect Windows builds.
 * @param parentJob Parent job for the downloader. If provided, the downloader will be cancelled when the parent job is cancelled.
 *
 * @author Oxters
 * @author Lossy
 * @since Oct 29, 2024
 */
@OptIn(ExperimentalCoroutinesApi::class)
@Suppress("unused")
class DepotDownloader @JvmOverloads constructor(
    private val steamClient: SteamClient,
    private val licenses: List<License>,
    private val debug: Boolean = false,
    private val useLanCache: Boolean = false,
    private var maxDownloads: Int = 8,
    private var maxFileWrites: Int = 1,
    private val androidEmulation: Boolean = false,
    private val parentJob: Job? = null,
) : Closeable {

    companion object {
        const val INVALID_APP_ID: Int = Int.MAX_VALUE
        const val INVALID_DEPOT_ID: Int = Int.MAX_VALUE
        const val INVALID_MANIFEST_ID: Long = Long.MAX_VALUE

        const val CONFIG_DIR: String = ".DepotDownloader"
        const val DEFAULT_BRANCH: String = "public"
        const val DEFAULT_DOWNLOAD_DIR: String = "depots"

        val STAGING_DIR: Path = CONFIG_DIR.toPath() / "staging"

        private const val MAX_DOWNLOAD_ATTEMPTS: Int = 10

        private fun retryDelayMillis(failures: Int): Long = (500L * failures).coerceAtMost(5_000L)

        private fun fraction(done: Long, total: Long): Float = if (total <= 0L) 1f else done.toFloat() / total

        private val SupportedWorkshopFileTypes: Set<EWorkshopFileType> = setOf(
            // First and Community share the value 0, and EWorkshopFileType.from(0) returns First.
            EWorkshopFileType.First,
            EWorkshopFileType.Community,
            EWorkshopFileType.Art,
            EWorkshopFileType.Screenshot,
            EWorkshopFileType.Merch,
            EWorkshopFileType.IntegratedGuide,
            EWorkshopFileType.ControllerBinding,
        )
    }

    private val completionFuture = CompletableFuture<Void>()

    private val filesystem: FileSystem by lazy { FileSystem.SYSTEM }

    private val listeners = CopyOnWriteArrayList<IDownloadListener>()

    private val progressUpdateInterval = 500L // ms

    // Errors are always logged. Everything else goes through [logger], which is only set with [debug].
    private val log: Logger = LogManager.getLogger(DepotDownloader::class.java)

    private val logger: Logger? = if (debug) log else null

    // Last line of defence: a failure inside the downloader must never take the host process down.
    private val scope: CoroutineScope = CoroutineScope(
        Dispatchers.IO +
            SupervisorJob(parentJob) +
            CoroutineExceptionHandler { _, e -> log.error("Uncaught error in DepotDownloader: ${e.message}", e) }
    )

    private var cdnClientPool: CDNClientPool? = null

    // Derived from the SteamClient's HTTP client on first use, see [HttpClient]. Built late on
    // purpose: a detected LAN cache raises maxDownloads before the first item is processed.
    private var ownHttpClient: OkHttpClient? = null

    private val httpClientLock = Any()

    private fun httpClient(): OkHttpClient = synchronized(httpClientLock) {
        ownHttpClient ?: HttpClient.create(
            base = steamClient.configuration.httpClient,
            maxDownloads = maxDownloads,
        ).also { ownHttpClient = it }
    }

    private var config: Config = Config(androidEmulation = androidEmulation)

    private var processingChannel = Channel<DownloadItem>(Channel.UNLIMITED)

    // Bounded channel — send() suspends producers when full, creating backpressure through the pipeline.
    private val networkChunkChannel = Channel<NetworkChunkItem>(64)

    // Half of available processors, clamped to at least 1. Leaves remaining cores for the OS and app.
    private val maxDecompress: Int = (Runtime.getRuntime().availableProcessors() / 2).coerceAtLeast(1)

    private var chunkProcessingJob: Job? = null

    private var steam3: Steam3Session? = null

    private var processingItemsMap = mutableMapOf<Int, DownloadItem>()

    // Lazily built on first accountHasAccess call; all app/depot IDs the account can access.
    private var accessibleIds: Set<Int>? = null

    // region [REGION] Private data classes.

    /**
     * Failure state of one queued item. The chunk pipeline outlives items, so every chunk carries
     * the session it belongs to: once a session has failed its remaining chunks are dropped instead
     * of downloaded, and whoever waits on the item is woken with the error.
     */
    private class DownloadSession {
        private val error = AtomicReference<Throwable?>(null)
        private val failure = CompletableDeferred<Nothing>()

        val isFailed: Boolean get() = error.get() != null

        fun fail(cause: Throwable) {
            if (error.compareAndSet(null, cause)) {
                failure.completeExceptionally(cause)
            }
        }

        fun throwIfFailed() {
            error.get()?.let { throw it }
        }

        /** Suspends until every chunk of [tracker] went through the pipeline, or throws once the session fails. */
        suspend fun awaitDrained(tracker: DepotChunkTracker) {
            select {
                tracker.drained.onAwait { }
                failure.onAwait { }
            }
            throwIfFailed()
        }
    }

    /** Counts the chunks of one depot that are still somewhere in the pipeline. */
    private class DepotChunkTracker(val session: DownloadSession) {
        // Starts at 1: a sentinel held while files are still being enumerated, so the count
        // cannot reach zero before every chunk has been queued.
        private val pending = AtomicInteger(1)

        val drained = CompletableDeferred<Unit>()

        fun chunkQueued() {
            pending.incrementAndGet()
        }

        /** Must be called exactly once per queued chunk (done, failed or dropped), and once for the sentinel. */
        fun chunkDone() {
            if (pending.decrementAndGet() == 0) {
                drained.complete(Unit)
            }
        }
    }

    private data class NetworkChunkItem(
        val tracker: DepotChunkTracker,
        val downloadCounter: GlobalDownloadCounter,
        val depotFilesData: DepotFilesData,
        val fileStreamData: FileStreamData,
        val fileData: FileData,
        val chunk: ChunkData,
        val totalChunksForFile: Int,
    )

    @Suppress("ArrayInDataClass")
    private data class DecompressItem(
        val tracker: DepotChunkTracker,
        val depot: DepotDownloadInfo,
        val depotDownloadCounter: DepotDownloadCounter,
        val downloadCounter: GlobalDownloadCounter,
        val file: FileData,
        val fileStreamData: FileStreamData,
        val chunk: ChunkData,
        val chunkBuffer: ByteArray,
    )

    @Suppress("ArrayInDataClass")
    private data class FileWriteItem(
        val tracker: DepotChunkTracker,
        val depot: DepotDownloadInfo,
        val depotDownloadCounter: DepotDownloadCounter,
        val downloadCounter: GlobalDownloadCounter,
        val file: FileData,
        val fileStreamData: FileStreamData,
        val chunk: ChunkData,
        val decompressed: Int,
        val decompressedBuffer: ByteArray,
    )

    private data class DirectoryResult(val success: Boolean, val installDir: Path?)

    private data class Config(
        val installPath: Path? = null,
        val betaPassword: String? = null,
        val downloadAllPlatforms: Boolean = false,
        val downloadAllArchs: Boolean = false,
        val downloadAllLanguages: Boolean = false,
        val androidEmulation: Boolean = false,
        val downloadManifestOnly: Boolean = false,
        val installToGameNameDirectory: Boolean = false,

        // Not used yet in code
        val usingFileList: Boolean = false,
        var filesToDownloadRegex: List<Regex> = emptyList(),
        var filesToDownload: HashSet<String> = hashSetOf(),
        var verifyAll: Boolean = false,
    )

    // endregion

    init {
        steam3 = Steam3Session(steamClient, debug)

        logger?.debug("DepotDownloader launched with ${licenses.size} for account")
        licenses.forEach { license ->
            if (license.accessToken.toULong() > 0UL) {
                steam3!!.packageTokens[license.packageID] = license.accessToken
            }
        }

        // Launch the processing loop
        scope.launch {
            processItems()
        }
    }

    private fun createChunkProcessingFlow() = networkChunkChannel.receiveAsFlow()
        .flatMapMerge(concurrency = maxDownloads) { item ->
            flow {
                val downloaded = runChunkStage(item.tracker, "downloading") {
                    downloadSteam3DepotFileChunk(
                        tracker = item.tracker,
                        downloadCounter = item.downloadCounter,
                        depotFilesData = item.depotFilesData,
                        file = item.fileData,
                        fileStreamData = item.fileStreamData,
                        chunk = item.chunk
                    )
                }
                if (downloaded != null) emit(downloaded)
            }.flowOn(Dispatchers.IO)
        }
        .flatMapMerge(concurrency = maxDecompress) { item ->
            flow {
                val decompressed = runChunkStage(item.tracker, "decompressing") { processFileDecompress(item) }
                if (decompressed != null) emit(decompressed)
            }.flowOn(Dispatchers.Default)
        }
        .flatMapMerge(concurrency = maxFileWrites) { item ->
            flow {
                val written = runChunkStage(item.tracker, "writing") { processFileWrites(item) }
                if (written != null) item.tracker.chunkDone()
                emit(Unit)
            }.flowOn(Dispatchers.IO)
        }

    /**
     * Runs one pipeline stage for a chunk. Returns null when the chunk left the pipeline here: either
     * its item had already failed (the chunk is dropped), or [block] threw, which fails the item.
     * Catches [Throwable] on purpose: a missing optional dependency surfaces as [NoClassDefFoundError]
     * and must fail one download, not the pipeline or the process.
     */
    private suspend inline fun <T : Any> runChunkStage(tracker: DepotChunkTracker, stage: String, block: () -> T): T? {
        if (tracker.session.isFailed) {
            tracker.chunkDone()
            return null
        }

        try {
            return block()
        } catch (e: Throwable) {
            // Only a cancellation of the downloader itself may stop the pipeline.
            if (e is CancellationException) currentCoroutineContext().ensureActive()

            log.error("Error $stage chunk: ${e.message}", e)

            val cause = if (e is CancellationException) DepotDownloaderException("Timed out $stage chunk", e) else e
            tracker.session.fail(cause)
            tracker.chunkDone()
            return null
        }
    }

    // region [REGION] Downloading Operations

    private suspend fun processPublishedFile(
        appId: Int,
        publishedFileId: Long,
        fileUrls: MutableList<Pair<String, String>>,
        contentFileIds: MutableList<Long>,
        skipReasons: MutableList<String>,
    ) {
        val details = steam3!!.getPublishedFileDetails(appId, PublishedFileID(publishedFileId))
        if (details == null) {
            val reason = "Unable to get the details of published file $publishedFileId"
            log.error(reason)
            skipReasons.add(reason)
            return
        }

        val fileType = EWorkshopFileType.from(details.fileType)

        if (fileType == EWorkshopFileType.Collection) {
            details.childrenList.forEach { child ->
                processPublishedFile(appId, child.publishedfileid, fileUrls, contentFileIds, skipReasons)
            }
        } else if (SupportedWorkshopFileTypes.contains(fileType)) {
            if (details.fileUrl.isNotEmpty()) {
                fileUrls.add(Pair(details.filename, details.fileUrl))
            } else if (details.hcontentFile > 0) {
                contentFileIds.add(details.hcontentFile)
            } else {
                val reason = "Unable to locate manifest ID for published file $publishedFileId"
                log.error(reason)
                skipReasons.add(reason)
            }
        } else {
            val reason = "Published file $publishedFileId has unsupported file type $fileType"
            log.error("$reason. Skipping file")
            skipReasons.add(reason)
        }
    }

    @Throws(IllegalStateException::class)
    private suspend fun downloadPubFile(appId: Int, publishedFileId: Long) {
        val fileUrls = mutableListOf<Pair<String, String>>()
        val contentFileIds = mutableListOf<Long>()
        val skipReasons = mutableListOf<String>()

        processPublishedFile(appId, publishedFileId, fileUrls, contentFileIds, skipReasons)

        // Without this an item with nothing to download would end without any callback.
        if (fileUrls.isEmpty() && contentFileIds.isEmpty()) {
            throw DepotDownloaderException(
                "Nothing to download for published file $publishedFileId: ${skipReasons.joinToString("; ")}"
            )
        }

        fileUrls.forEach { item ->
            downloadWebFile(appId, item.first, item.second)
        }

        if (contentFileIds.isEmpty()) {
            // Only web files: downloadApp, which reports the completion otherwise, is not reached.
            finishDepotDownload(appId)
        } else {
            val depotManifestIds = contentFileIds.map { id -> appId to id }
            downloadApp(
                appId = appId,
                depotManifestIds = depotManifestIds,
                branch = DEFAULT_BRANCH,
                os = null,
                arch = null,
                language = null,
                lv = false,
                isUgc = true
            )
        }
    }

    private suspend fun downloadUGC(
        appId: Int,
        ugcId: Long,
    ) {
        var details: UGCDetailsCallback? = null

        val steamUser = requireNotNull(steam3!!.steamUser)
        val steamId = requireNotNull(steamUser.steamID)

        if (steamId.accountType != EAccountType.AnonUser) {
            val ugcHandle = UGCHandle(ugcId)
            details = steam3!!.getUGCDetails(ugcHandle)
        } else {
            log.error("Unable to query UGC details for $ugcId from an anonymous account")
        }

        if (!details?.url.isNullOrBlank()) {
            downloadWebFile(appId = appId, fileName = details.fileName, url = details.url)
            // downloadApp, which reports the completion otherwise, is not reached.
            finishDepotDownload(appId)
        } else {
            downloadApp(
                appId = appId,
                depotManifestIds = listOf(appId to ugcId),
                branch = DEFAULT_BRANCH,
                os = null,
                arch = null,
                language = null,
                lv = false,
                isUgc = true,
            )
        }
    }

    @Throws(IllegalStateException::class, IOException::class)
    private suspend fun downloadWebFile(appId: Int, fileName: String, url: String) {
        val (success, installDir) = createDirectories(appId, 0, appId)

        // Failures are thrown so the item ends in onDownloadFailed instead of ending silently.
        if (!success) {
            throw DepotDownloaderException("Unable to create install directories for $fileName")
        }

        val stagingDir = installDir!! / "staging"
        val fileStagingPath = stagingDir / fileName
        val fileFinalPath = installDir / fileName

        filesystem.createDirectories(fileFinalPath.parent!!)
        filesystem.createDirectories(fileStagingPath.parent!!)

        logger?.debug("Starting download of $fileName...")

        // The same HTTP client chunks and manifests use.
        val bytesWritten = HttpClient.downloadFile(
            httpClient = httpClient(),
            url = url,
            destination = fileStagingPath,
            filesystem = filesystem,
        )

        logger?.debug("Download completed, ${Util.formatBytes(bytesWritten)}.")

        if (filesystem.exists(fileFinalPath)) {
            logger?.debug("Deleting $fileFinalPath")
            filesystem.delete(fileFinalPath)
        }

        try {
            filesystem.atomicMove(fileStagingPath, fileFinalPath)
            logger?.debug("File '$fileStagingPath' moved to final location: $fileFinalPath")
        } catch (e: IOException) {
            log.error("Failed to move files", e)
            throw e
        }
    }

    // L4D2 (app) supports LV
    @Throws(IllegalStateException::class, DepotDownloaderException::class)
    private suspend fun downloadApp(
        appId: Int,
        depotManifestIds: List<Pair<Int, Long>>,
        branch: String,
        os: String?,
        arch: String?,
        language: String?,
        lv: Boolean,
        isUgc: Boolean,
    ) {
        var depotManifestIds = depotManifestIds.toMutableList()

        val steamUser = requireNotNull(steam3!!.steamUser)
        cdnClientPool = CDNClientPool(steam3!!, appId, scope, debug, httpClient())

        // Load our configuration data containing the depots currently installed
        var configPath = config.installPath
        if (configPath == null) {
            configPath = DEFAULT_DOWNLOAD_DIR.toPath()
        }

        filesystem.createDirectories(configPath)
        DepotConfigStore.loadFromFile(configPath / CONFIG_DIR / "depot.config")

        steam3!!.requestAppInfo(appId)

        if (!accountHasAccess(appId, appId)) {
            if (steamUser.steamID!!.accountType != EAccountType.AnonUser && steam3!!.requestFreeAppLicense(appId)) {
                logger?.debug("Obtained FreeOnDemand license for app $appId")

                // Fetch app info again in case we didn't get it fully without a license.
                steam3!!.requestAppInfo(appId, true)
            } else {
                val contentName = getAppName(appId)
                throw DepotDownloaderException("App $appId ($contentName) is not available from this account.")
            }
        }

        val hasSpecificDepots = depotManifestIds.isNotEmpty()
        val depotIdsFound = mutableListOf<Int>()
        val depotIdsExpected = depotManifestIds.map { x -> x.first }.toMutableList()
        val depots = getSteam3AppSection(appId, EAppInfoSection.Depots)

        if (isUgc) {
            val workshopDepot = depots!!["workshopdepot"].asInteger()
            if (workshopDepot != 0 && !depotIdsExpected.contains(workshopDepot)) {
                depotIdsExpected.add(workshopDepot)
                depotManifestIds = depotManifestIds.map { pair -> workshopDepot to pair.second }.toMutableList()
            }

            depotIdsFound.addAll(depotIdsExpected)
        } else {
            logger?.debug("Using app branch: $branch")

            depots?.children?.forEach { depotSection ->
                if (depotSection.children.isEmpty()) {
                    return@forEach
                }

                val id: Int = depotSection.name?.toIntOrNull() ?: return@forEach

                if (hasSpecificDepots && !depotIdsExpected.contains(id)) {
                    return@forEach
                }

                if (!hasSpecificDepots) {
                    val depotConfig = depotSection["config"]
                    if (depotConfig != KeyValue.INVALID) {
                        if (!config.downloadAllPlatforms &&
                            depotConfig["oslist"] != KeyValue.INVALID &&
                            !depotConfig["oslist"].value.isNullOrBlank()
                        ) {
                            val osList = depotConfig["oslist"].value!!.split(",")
                            if (osList.indexOf(os ?: Util.getSteamOS(config.androidEmulation)) == -1) {
                                return@forEach
                            }
                        }

                        if (!config.downloadAllArchs &&
                            depotConfig["osarch"] != KeyValue.INVALID &&
                            !depotConfig["osarch"].value.isNullOrBlank()
                        ) {
                            val depotArch = depotConfig["osarch"].value
                            if (depotArch != (arch ?: Util.getSteamArch())) {
                                return@forEach
                            }
                        }

                        if (!config.downloadAllLanguages &&
                            depotConfig["language"] != KeyValue.INVALID &&
                            !depotConfig["language"].value.isNullOrBlank()
                        ) {
                            val depotLang = depotConfig["language"].value
                            if (depotLang != (language ?: "english")) {
                                return@forEach
                            }
                        }

                        if (!lv &&
                            depotConfig["lowviolence"] != KeyValue.INVALID &&
                            depotConfig["lowviolence"].asBoolean()
                        ) {
                            return@forEach
                        }
                    }
                }

                depotIdsFound.add(id)

                if (!hasSpecificDepots) {
                    depotManifestIds.add(id to INVALID_MANIFEST_ID)
                }
            }

            if (depotManifestIds.isEmpty() && !hasSpecificDepots) {
                throw DepotDownloaderException("Couldn't find any depots to download for app $appId")
            }

            if (depotIdsFound.size < depotIdsExpected.size) {
                val remainingDepotIds = depotIdsExpected.subtract(depotIdsFound.toSet())
                throw DepotDownloaderException("Depot ${remainingDepotIds.joinToString(", ")} not listed for app $appId")
            }
        }

        val infos = mutableListOf<DepotDownloadInfo>()
        val skipReasons = mutableListOf<String>()

        depotManifestIds.forEach { (depotId, manifestId) ->
            val info = getDepotInfo(depotId, appId, manifestId, branch, skipReasons)
            if (info != null) {
                infos.add(info)
            }
        }

        // Without this an item whose depots were all skipped would be reported as completed.
        if (infos.isEmpty()) {
            throw DepotDownloaderException(
                "None of the depots of app $appId could be downloaded: ${skipReasons.joinToString("; ")}"
            )
        }

        downloadSteam3(appId, infos)
    }

    @Throws(IllegalStateException::class)
    private suspend fun getDepotInfo(
        depotId: Int,
        appId: Int,
        manifestId: Long,
        branch: String,
        skipReasons: MutableList<String>,
    ): DepotDownloadInfo? {
        var manifestId = manifestId
        var branch = branch

        fun skip(reason: String): DepotDownloadInfo? {
            skipReasons.add(reason)
            skipDepot(depotId, reason)
            return null
        }

        if (appId != INVALID_APP_ID) {
            steam3!!.requestAppInfo(appId)
        }

        if (!accountHasAccess(appId, depotId)) {
            return skip("Depot $depotId is not available from this account.")
        }

        if (manifestId == INVALID_MANIFEST_ID) {
            manifestId = getSteam3DepotManifest(depotId, appId, branch)

            if (manifestId == INVALID_MANIFEST_ID && !branch.equals(DEFAULT_BRANCH, true)) {
                log.error("Warning: Depot $depotId does not have branch named \"$branch\". Trying $DEFAULT_BRANCH branch.")
                branch = DEFAULT_BRANCH
                manifestId = getSteam3DepotManifest(depotId, appId, branch)
            }

            if (manifestId == INVALID_MANIFEST_ID) {
                return skip("Depot $depotId missing public subsection or manifest section.")
            }
        }

        steam3!!.requestDepotKey(depotId, appId)

        val depotKey = steam3!!.depotKeys[depotId]
            ?: return skip("No valid depot key for $depotId, unable to download.")

        val uVersion = getSteam3AppBuildNumber(appId, branch)

        val (success, installDir) = createDirectories(depotId, uVersion, appId)
        if (!success) {
            return skip("Unable to create install directories for depot $depotId.")
        }

        // For depots that are proxied through depotfromapp, we still need to resolve the proxy app id, unless the app is freetodownload
        var containingAppId = appId
        val proxyAppId = getSteam3DepotProxyAppId(depotId, appId)
        if (proxyAppId != INVALID_APP_ID) {
            val common = getSteam3AppSection(appId, EAppInfoSection.Common)
            if (common == null || !common["FreeToDownload"].asBoolean()) {
                containingAppId = proxyAppId
            }
        }

        return DepotDownloadInfo(
            depotId = depotId,
            appId = containingAppId,
            manifestId = manifestId,
            branch = branch,
            installDir = installDir!!,
            depotKey = depotKey
        )
    }

    /** A depot is left out of the download. Always logged, and reported to listeners. */
    private fun skipDepot(depotId: Int, reason: String) {
        log.error(reason)
        notifyListeners { it.onDepotSkipped(depotId, reason) }
    }

    private suspend fun getSteam3DepotManifest(
        depotId: Int,
        appId: Int,
        branch: String,
    ): Long {
        val depots = getSteam3AppSection(appId, EAppInfoSection.Depots)
        var depotChild = depots?.get(depotId.toString()) ?: KeyValue.INVALID

        if (depotChild == KeyValue.INVALID) {
            return INVALID_MANIFEST_ID
        }

        // Shared depots can either provide manifests, or leave you relying on their parent app.
        // It seems that with the latter, "sharedinstall" will exist (and equals 2 in the one existance I know of).
        // Rather than relay on the unknown sharedinstall key, just look for manifests. Test cases: 111710, 346680.
        if (depotChild["manifests"] == KeyValue.INVALID && depotChild["depotfromapp"] != KeyValue.INVALID) {
            val otherAppId = depotChild["depotfromapp"].asInteger()
            if (otherAppId == appId) {
                // This shouldn't ever happen, but ya never know with Valve. Don't infinite loop.
                log.error("App $appId, Depot $depotId has depotfromapp of $otherAppId!")
                return INVALID_MANIFEST_ID
            }

            steam3!!.requestAppInfo(otherAppId)

            return getSteam3DepotManifest(depotId, otherAppId, branch)
        }

        var manifests = depotChild["manifests"]

        if (manifests.children.isEmpty()) {
            return INVALID_MANIFEST_ID
        }

        var node = manifests[branch]["gid"]

        // Non passworded branch, found the manifest
        if (node.value != null) {
            return node.value!!.toLong()
        }

        // If we requested public branch, and it had no manifest, nothing to do
        if (branch.equals(DEFAULT_BRANCH, true)) {
            return INVALID_MANIFEST_ID
        }

        // Either the branch just doesn't exist, or it has a password
        if (config.betaPassword.isNullOrBlank()) {
            log.error("Branch $branch for depot $depotId was not found, either it does not exist or it has a password.")
            return INVALID_MANIFEST_ID
        }

        if (!steam3!!.appBetaPasswords.containsKey(branch)) {
            // Submit the password to Steam now to get encryption keys
            steam3!!.checkAppBetaPassword(appId, config.betaPassword!!)

            if (!steam3!!.appBetaPasswords.containsKey(branch)) {
                log.error("Error: Password was invalid for branch $branch (or the branch does not exist)")
                return INVALID_MANIFEST_ID
            }
        }

        // Got the password, request private depot section
        // TODO: (SK) We're probably repeating this request for every depot?
        val privateDepotSection = steam3!!.getPrivateBetaDepotSection(appId, branch)

        // Now repeat the same code to get the manifest gid from depot section
        depotChild = privateDepotSection[depotId.toString()]

        if (depotChild == KeyValue.INVALID) {
            return INVALID_MANIFEST_ID
        }

        manifests = depotChild["manifests"]

        if (manifests.children.isEmpty()) {
            return INVALID_MANIFEST_ID
        }

        node = manifests[branch]["gid"]

        if (node.value == null) {
            return INVALID_MANIFEST_ID
        }

        return node.value!!.toLong()
    }

    private fun getSteam3AppBuildNumber(appId: Int, branch: String): Int {
        if (appId == INVALID_APP_ID) {
            return 0
        }

        val depots = getSteam3AppSection(appId, EAppInfoSection.Depots) ?: KeyValue.INVALID
        val branches = depots["branches"]
        val node = branches[branch]

        if (node == KeyValue.INVALID) {
            return 0
        }

        val buildId = node["buildid"]

        if (buildId == KeyValue.INVALID) {
            return 0
        }

        return buildId.value!!.toInt()
    }

    private fun getSteam3DepotProxyAppId(depotId: Int, appId: Int): Int {
        val depots = getSteam3AppSection(appId, EAppInfoSection.Depots) ?: KeyValue.INVALID
        val depotChild = depots[depotId.toString()]

        if (depotChild == KeyValue.INVALID) {
            return INVALID_APP_ID
        }

        if (depotChild["depotfromapp"] == KeyValue.INVALID) {
            return INVALID_APP_ID
        }

        return depotChild["depotfromapp"].asInteger()
    }

    @Throws(IllegalStateException::class, IOException::class)
    private fun createDirectories(depotId: Int, depotVersion: Int, appId: Int = 0): DirectoryResult {
        var installDir: Path?
        try {
            if (config.installPath?.toString().isNullOrBlank()) {
                // Android Check
                if (SystemUtils.IS_OS_ANDROID) {
                    // This should propagate up to the caller.
                    throw IllegalStateException("Android must have an installation directory set.")
                }

                filesystem.createDirectories(DEFAULT_DOWNLOAD_DIR.toPath())

                if (config.installToGameNameDirectory) {
                    val info = getSteam3AppSection(appId, EAppInfoSection.Config) ?: KeyValue.INVALID
                    val appInstallDir = info["installdir"].asString()

                    if (appInstallDir.isNullOrBlank()) {
                        throw IOException("Config install directory is blank, cannot create directory")
                    }

                    installDir = DEFAULT_DOWNLOAD_DIR.toPath() / appInstallDir

                    filesystem.createDirectories(installDir)
                } else {
                    val depotPath = DEFAULT_DOWNLOAD_DIR.toPath() / depotId.toString()
                    filesystem.createDirectories(depotPath)

                    installDir = depotPath / depotVersion.toString()
                    filesystem.createDirectories(installDir)
                }

                filesystem.createDirectories(installDir / CONFIG_DIR)
                filesystem.createDirectories(installDir / STAGING_DIR)
            } else {
                filesystem.createDirectories(config.installPath!!)

                if (config.installToGameNameDirectory) {
                    val info = getSteam3AppSection(appId, EAppInfoSection.Config) ?: KeyValue.INVALID
                    val appInstallDir = info["installdir"].asString()

                    if (appInstallDir.isNullOrBlank()) {
                        throw IOException("Config install directory is blank, cannot create directory")
                    }

                    installDir = config.installPath!! / appInstallDir

                    filesystem.createDirectories(installDir)
                } else {
                    installDir = config.installPath!!
                }

                filesystem.createDirectories(installDir / CONFIG_DIR)
                filesystem.createDirectories(installDir / STAGING_DIR)
            }
        } catch (e: IOException) {
            log.error("Failed to create directory for depot $depotId, app $appId", e)
            return DirectoryResult(false, null)
        }

        return DirectoryResult(true, installDir)
    }

    private fun getAppName(appId: Int): String {
        val info = getSteam3AppSection(appId, EAppInfoSection.Common) ?: KeyValue.INVALID
        return info["name"].asString() ?: ""
    }

    private fun getSteam3AppSection(appId: Int, section: EAppInfoSection): KeyValue? {
        if (steam3 == null) {
            return null
        }

        if (steam3!!.appInfo.isEmpty()) {
            return null
        }

        val app = steam3!!.appInfo[appId]?.value ?: return null

        val appInfo = app.keyValues
        val sectionKey = when (section) {
            EAppInfoSection.Common -> "common"
            EAppInfoSection.Extended -> "extended"
            EAppInfoSection.Config -> "config"
            EAppInfoSection.Depots -> "depots"
            else -> throw DepotDownloaderException("${section.name} not implemented")
        }

        val sectionKV = appInfo.children.firstOrNull { c -> c.name == sectionKey }
        return sectionKV
    }

    private suspend fun accountHasAccess(appId: Int, depotId: Int): Boolean {
        val steamUser = requireNotNull(steam3!!.steamUser)
        val steamID = requireNotNull(steamUser.steamID)

        if (licenses.isEmpty() && steamID.accountType != EAccountType.AnonUser) {
            return false
        }

        if (accessibleIds == null) {
            val licenseQuery = if (steamID.accountType == EAccountType.AnonUser) {
                listOf(17906)
            } else {
                licenses.map { it.packageID }.distinct()
            }

            steam3!!.requestPackageInfo(licenseQuery)

            accessibleIds = buildSet {
                licenseQuery.forEach { license ->
                    steam3!!.packageInfo[license]?.value?.let { pkg ->
                        pkg.keyValues["appids"].children.forEach { add(it.asInteger()) }
                        pkg.keyValues["depotids"].children.forEach { add(it.asInteger()) }
                    }
                }
            }
        }

        if (depotId in accessibleIds!!) return true

        // Check if this app is free to download without a license
        val info = getSteam3AppSection(appId, EAppInfoSection.Common)

        return info != null && info["FreeToDownload"].asBoolean()
    }

    private suspend fun downloadSteam3(mainAppId: Int, depots: List<DepotDownloadInfo>): Unit = coroutineScope {
        cdnClientPool?.updateServerList(maxDownloads)

        val downloadCounter = GlobalDownloadCounter()
        val depotsToDownload = ArrayList<DepotFilesData>(depots.size)
        val allFileNamesAllDepots = hashSetOf<String>()

        // Fetch all depot manifests in parallel for faster startup
        depots.map { depot ->
            async { processDepotManifestAndFiles(depot, downloadCounter) }
        }.awaitAll().filterNotNull().forEach { depotFileData ->
            depotsToDownload.add(depotFileData)
            allFileNamesAllDepots.addAll(depotFileData.allFileNames)
        }

        // If we're about to write all the files to the same directory, we will need to first de-duplicate any files by path
        // This is in last-depot-wins order, from Steam or the list of depots supplied by the user
        if (config.installPath != null && depotsToDownload.isNotEmpty()) {
            val claimedFileNames = mutableSetOf<String>()
            for (i in depotsToDownload.indices.reversed()) {
                // For each depot, remove all files from the list that have been claimed by a later depot
                depotsToDownload[i].filteredFiles.removeAll { file -> file.fileName in claimedFileNames }
                claimedFileNames.addAll(depotsToDownload[i].allFileNames)
            }
        }

        val session = DownloadSession()
        try {
            depotsToDownload.forEach { depotFileData ->
                downloadSteam3DepotFiles(session, downloadCounter, depotFileData, allFileNamesAllDepots)
            }
        } catch (e: Throwable) {
            // Drops the chunks of this item that are still in the pipeline.
            session.fail(e)
            throw e
        }

        logger?.debug(
            "Total downloaded: ${downloadCounter.totalBytesCompressed.get()} bytes " +
                "(${downloadCounter.totalBytesUncompressed.get()} bytes uncompressed) from ${depots.size} depots"
        )

        finishDepotDownload(mainAppId)
    }

    private suspend fun processDepotManifestAndFiles(
        depot: DepotDownloadInfo,
        downloadCounter: GlobalDownloadCounter,
    ): DepotFilesData? = withContext(Dispatchers.IO) {
        val depotCounter = DepotDownloadCounter()

        logger?.debug("Processing depot ${depot.depotId}")

        var oldManifest: DepotManifest? = null

        @Suppress("VariableInitializerIsRedundant")
        var newManifest: DepotManifest? = null

        val configDir = depot.installDir / CONFIG_DIR

        @Suppress("VariableInitializerIsRedundant")
        var lastManifestId = INVALID_MANIFEST_ID
        lastManifestId = DepotConfigStore.getInstance().installedManifestIDs[depot.depotId] ?: INVALID_MANIFEST_ID

        // In case we have an early exit, this will force equiv of verifyall next run.
        DepotConfigStore.getInstance().installedManifestIDs[depot.depotId] = INVALID_MANIFEST_ID
        DepotConfigStore.save()

        if (lastManifestId != INVALID_MANIFEST_ID) {
            // We only have to show this warning if the old manifest ID was different
            val badHashWarning = lastManifestId != depot.manifestId
            oldManifest = Util.loadManifestFromFile(configDir, depot.depotId, lastManifestId, badHashWarning)
        }

        if (lastManifestId == depot.manifestId && oldManifest != null) {
            newManifest = oldManifest
            logger?.debug("Already have manifest ${depot.manifestId} for depot ${depot.depotId}.")
        } else {
            newManifest = Util.loadManifestFromFile(configDir, depot.depotId, depot.manifestId, true)

            if (newManifest != null) {
                logger?.debug("Already have manifest ${depot.manifestId} for depot ${depot.depotId}.")
            } else {
                logger?.debug("Downloading depot ${depot.depotId} manifest")
                notifyListeners { it.onStatusUpdate("Downloading manifest for depot ${depot.depotId}") }

                var manifestRequestCode: ULong = 0U
                var manifestRequestCodeExpiration = Instant.MIN

                var failures = 0
                var lastError: Throwable? = null

                do {
                    ensureActive()

                    if (failures >= MAX_DOWNLOAD_ATTEMPTS) break
                    if (failures > 0) delay(retryDelayMillis(failures))

                    var connection: Server? = null

                    try {
                        connection = cdnClientPool!!.getConnection()

                        var cdnToken: String? = null

                        val authTokenCallbackPromise = steam3!!.cdnAuthTokens[depot.depotId to connection.host]
                        if (authTokenCallbackPromise != null) {
                            try {
                                val result = authTokenCallbackPromise.await()
                                cdnToken = result.token
                            } catch (e: Exception) {
                                ensureActive()
                                log.error("Failed to get CDN auth token: ${e.message}")
                            }
                        }

                        val now = Instant.now()

                        // In order to download this manifest, we need the current manifest request code
                        // The manifest request code is only valid for a specific period in time
                        if (manifestRequestCode == 0UL || now >= manifestRequestCodeExpiration) {
                            manifestRequestCode = steam3!!.getDepotManifestRequestCode(
                                depotId = depot.depotId,
                                appId = depot.appId,
                                manifestId = depot.manifestId,
                                branch = depot.branch,
                            )

                            // This code will hopefully be valid for one period following the issuing period
                            manifestRequestCodeExpiration = now.plus(5, ChronoUnit.MINUTES)

                            // If we could not get the manifest code, this is a fatal error
                            if (manifestRequestCode == 0UL) {
                                skipDepot(depot.depotId, "No manifest request code for depot ${depot.depotId}, manifest ${depot.manifestId}.")
                                return@withContext null
                            }
                        }

                        logger?.debug("Downloading manifest ${depot.manifestId} from $connection with ${cdnClientPool!!.proxyServer ?: "no proxy"}")

                        newManifest = cdnClientPool!!.cdnClient!!.downloadManifest(
                            depotId = depot.depotId,
                            manifestId = depot.manifestId,
                            manifestRequestCode = manifestRequestCode,
                            server = connection,
                            depotKey = depot.depotKey,
                            proxyServer = cdnClientPool!!.proxyServer,
                            cdnAuthToken = cdnToken,
                        )

                        cdnClientPool!!.returnConnection(connection)
                    } catch (e: SteamKitWebRequestException) {
                        // If the CDN returned 403, attempt to get a cdn auth if we didn't yet
                        if (e.statusCode == 403 && !steam3!!.cdnAuthTokens.containsKey(depot.depotId to connection!!.host)) {
                            steam3!!.requestCDNAuthToken(depot.appId, depot.depotId, connection)

                            cdnClientPool!!.returnConnection(connection)

                            continue
                        }

                        cdnClientPool!!.skipConnection(connection)

                        failures++
                        lastError = e

                        // Unauthorized || Forbidden
                        if (e.statusCode == 401 || e.statusCode == 403) {
                            log.error("Encountered ${depot.depotId} for depot manifest ${depot.manifestId} ${e.statusCode}. Aborting.")
                            break
                        }

                        // NotFound
                        if (e.statusCode == 404) {
                            log.error("Encountered 404 for depot manifest ${depot.depotId} ${depot.manifestId}. Aborting.")
                            break
                        }

                        log.error("Encountered error downloading depot manifest ${depot.depotId} ${depot.manifestId}: ${e.statusCode}")
                    } catch (e: Exception) {
                        // A real cancellation stops here; a timeout reported as one is retried like any error.
                        if (e is CancellationException) ensureActive()

                        if (e is java.net.UnknownHostException || e.cause is java.net.UnknownHostException) {
                            cdnClientPool!!.returnBrokenConnection(connection)
                        } else {
                            cdnClientPool!!.skipConnection(connection)
                        }

                        failures++
                        lastError = e

                        log.error("Encountered error downloading manifest for depot ${depot.depotId} ${depot.manifestId}: ${e.message}")
                        if (!cdnClientPool!!.hasServers()) break
                    }
                } while (newManifest == null)

                if (newManifest == null) {
                    throw DepotDownloaderException(
                        "Unable to download manifest ${depot.manifestId} for depot ${depot.depotId}",
                        lastError
                    )
                }

                Util.saveManifestToFile(configDir, newManifest)
            }
        }

        logger?.debug("Manifest ${depot.manifestId} (${newManifest.creationTime})")

        if (config.downloadManifestOnly) {
            Util.dumpManifestToTextFile(depot, newManifest)
            return@withContext null
        }

        val stagingDir = depot.installDir / STAGING_DIR

        val filesAfterExclusions = newManifest.files.filter { testIsFileIncluded(it.fileName) }
        val allFileNames = HashSet<String>(filesAfterExclusions.size)

        // Pre-process
        filesAfterExclusions.forEachIndexed { index, file ->
            if (index % 50 == 0) {
                ensureActive() // Check cancellation periodically
            }

            allFileNames.add(file.fileName)

            val fileFinalPath = depot.installDir / file.fileName
            val fileStagingPath = stagingDir / file.fileName

            if (file.flags.contains(EDepotFileFlag.Directory)) {
                filesystem.createDirectories(fileFinalPath)
                filesystem.createDirectories(fileStagingPath)
            } else {
                // Some manifests don't explicitly include all necessary directories
                filesystem.createDirectories(fileFinalPath.parent!!)
                filesystem.createDirectories(fileStagingPath.parent!!)

                downloadCounter.completeDownloadSize.addAndGet(file.totalSize)
                depotCounter.completeDownloadSize += file.totalSize
            }
        }

        return@withContext DepotFilesData(
            depotDownloadInfo = depot,
            depotCounter = depotCounter,
            stagingDir = stagingDir,
            manifest = newManifest,
            previousManifest = oldManifest,
            filteredFiles = filesAfterExclusions.toMutableList(),
            allFileNames = allFileNames,
            previousManifestIndex = oldManifest?.files?.associateBy { it.fileName },
        )
    }

    private suspend fun downloadSteam3DepotFiles(
        session: DownloadSession,
        downloadCounter: GlobalDownloadCounter,
        depotFilesData: DepotFilesData,
        allFileNamesAllDepots: HashSet<String>,
    ) = withContext(Dispatchers.IO) {
        val depot = depotFilesData.depotDownloadInfo
        val depotCounter = depotFilesData.depotCounter

        logger?.debug("Downloading depot ${depot.depotId}")

        val files = depotFilesData.filteredFiles.filter { !it.flags.contains(EDepotFileFlag.Directory) }

        val tracker = DepotChunkTracker(session)

        coroutineScope {
            files.map { file ->
                async {
                    downloadSteam3DepotFile(
                        tracker = tracker,
                        downloadCounter = downloadCounter,
                        depotFilesData = depotFilesData,
                        file = file,
                    )
                }
            }.awaitAll()
        }

        // Every chunk is queued: release the sentinel and wait until the last one is on disk.
        // The depot must not be recorded as installed (below) before that, or an interrupted
        // download would be trusted on the next run without its missing chunks being noticed.
        tracker.chunkDone()
        session.awaitDrained(tracker)

        logger?.debug("All chunks completed for depot ${depot.depotId}")

        // Check for deleted files if updating the depot.
        if (depotFilesData.previousManifest != null) {
            val previousFilteredFiles = depotFilesData.previousManifest.files
                .filter { testIsFileIncluded(it.fileName) }
                .map { it.fileName }
                .toHashSet()

            // Check if we are writing to a single output directory. If not, each depot folder is managed independently
            if (config.installPath == null) {
                // Of the list of files in the previous manifest, remove any file names that exist in the current set of all file names
                previousFilteredFiles.removeAll(depotFilesData.allFileNames)
            } else {
                // Of the list of files in the previous manifest, remove any file names that exist in the current set of all file names across all depots being downloaded
                previousFilteredFiles.removeAll(allFileNamesAllDepots)
            }

            previousFilteredFiles.forEach { existingFileName ->
                val fileFinalPath = depot.installDir / existingFileName

                if (!filesystem.exists(fileFinalPath)) {
                    return@forEach
                }

                filesystem.delete(fileFinalPath)
                logger?.debug("Deleted $fileFinalPath")
            }
        }

        DepotConfigStore.getInstance().installedManifestIDs[depot.depotId] = depot.manifestId
        DepotConfigStore.save()

        // Notify depot completion
        notifyListeners { listener ->
            listener.onDepotCompleted(
                depotId = depot.depotId,
                compressedBytes = depotCounter.depotBytesCompressed,
                uncompressedBytes = depotCounter.depotBytesUncompressed
            )
        }

        logger?.debug("Depot ${depot.depotId} - Downloaded ${depotCounter.depotBytesCompressed} bytes (${depotCounter.depotBytesUncompressed} bytes uncompressed)")
    }

    private suspend fun downloadSteam3DepotFile(
        tracker: DepotChunkTracker,
        downloadCounter: GlobalDownloadCounter,
        depotFilesData: DepotFilesData,
        file: FileData,
    ) = withContext(Dispatchers.IO) {
        ensureActive()
        tracker.session.throwIfFailed()

        val depot = depotFilesData.depotDownloadInfo
        val stagingDir = depotFilesData.stagingDir
        val depotDownloadCounter = depotFilesData.depotCounter

        val oldManifestFile: FileData? = depotFilesData.previousManifestIndex?.get(file.fileName)

        val fileFinalPath = depot.installDir / file.fileName
        val fileStagingPath = stagingDir / file.fileName

        // This may still exist if the previous run exited before cleanup
        if (filesystem.exists(fileStagingPath)) {
            filesystem.delete(fileStagingPath)
        }

        var neededChunks: MutableList<ChunkData>? = null
        val fileDidExist = filesystem.exists(fileFinalPath)
        if (!fileDidExist) {
            logger?.debug("Pre-allocating: $fileFinalPath")
            notifyListeners { it.onStatusUpdate("Allocating file: ${file.fileName}") }

            // create new file. need all chunks
            try {
                // okio resize can OOM for large files on android.
                RandomAccessFile(fileFinalPath.toFile(), "rw").use {
                    it.setLength(file.totalSize)
                }
            } catch (e: IOException) {
                throw DepotDownloaderException("Failed to allocate file $fileFinalPath: ${e.message}")
            }

            neededChunks = ArrayList(file.chunks)
        } else {
            // open existing
            if (oldManifestFile != null) {
                neededChunks = arrayListOf()

                val hashMatches = oldManifestFile.fileHash.contentEquals(file.fileHash)
                if (config.verifyAll || !hashMatches) {
                    // we have a version of this file, but it doesn't fully match what we want
                    if (config.verifyAll) {
                        logger?.debug("Validating: $fileFinalPath")
                    }

                    val matchingChunks = arrayListOf<ChunkMatch>()

                    file.chunks.forEach { chunk ->
                        yield()

                        val oldChunk = oldManifestFile.chunks.firstOrNull { c ->
                            c.chunkID.contentEquals(chunk.chunkID)
                        }
                        if (oldChunk != null) {
                            val chunkMatch = ChunkMatch(oldChunk, chunk)
                            matchingChunks.add(chunkMatch)
                        } else {
                            neededChunks.add(chunk)
                        }
                    }

                    val orderedChunks = matchingChunks.sortedBy { x -> x.oldChunk.offset }

                    val copyChunks = arrayListOf<ChunkMatch>()

                    filesystem.openReadOnly(fileFinalPath).use { handle ->
                        orderedChunks.forEach { match ->
                            yield()

                            // Read the chunk data into a byte array
                            val length = match.oldChunk.uncompressedLength
                            val buffer = ByteArray(length)
                            handle.read(match.oldChunk.offset, buffer, 0, length)

                            if (Adler32.calculate(buffer) != match.oldChunk.checksum) {
                                neededChunks.add(match.newChunk)
                            } else {
                                copyChunks.add(match)
                            }
                        }
                    }

                    if (!hashMatches || neededChunks.isNotEmpty()) {
                        filesystem.atomicMove(fileFinalPath, fileStagingPath)

                        try {
                            RandomAccessFile(fileFinalPath.toFile(), "rw").use { raf ->
                                raf.setLength(file.totalSize)
                            }
                        } catch (ex: IOException) {
                            throw DepotDownloaderException(
                                "Failed to resize file to expected size $fileFinalPath: ${ex.message}"
                            )
                        }

                        filesystem.openReadOnly(fileStagingPath).use { oldHandle ->
                            filesystem.openReadWrite(fileFinalPath).use { newHandle ->
                                // okio resize can OOM for large files on android.
                                for (match in copyChunks) {
                                    ensureActive()

                                    val tmp = ByteArray(match.oldChunk.uncompressedLength)
                                    oldHandle.read(match.oldChunk.offset, tmp, 0, tmp.size)
                                    newHandle.write(match.newChunk.offset, tmp, 0, tmp.size)
                                }
                            }
                        }

                        filesystem.delete(fileStagingPath)
                    }
                }
            } else {
                // No old manifest or file not in old manifest. We must validate.
                val fileSize = filesystem.metadata(fileFinalPath).size ?: 0L
                if (fileSize.toULong() != file.totalSize.toULong()) {
                    try {
                        // okio resize can OOM for large files on android.
                        RandomAccessFile(fileFinalPath.toFile(), "rw").use { raf ->
                            raf.setLength(file.totalSize)
                        }
                    } catch (ex: IOException) {
                        throw DepotDownloaderException(
                            "Failed to allocate file $fileFinalPath: ${ex.message}"
                        )
                    }
                }

                filesystem.openReadWrite(fileFinalPath).use { handle ->
                    logger?.debug("Validating $fileFinalPath")
                    notifyListeners { it.onStatusUpdate("Validating: ${file.fileName}") }

                    neededChunks = Util.validateSteam3FileChecksums(
                        handle = handle,
                        chunkData = file.chunks.sortedBy { it.offset }
                    ).toMutableList()
                }
            }

            if (neededChunks!!.isEmpty()) {
                synchronized(depotDownloadCounter) {
                    depotDownloadCounter.sizeDownloaded += file.totalSize

                    val percentage =
                        (depotDownloadCounter.sizeDownloaded / depotDownloadCounter.completeDownloadSize.toFloat()) * 100.0f
                    logger?.debug("%.2f%% %s".format(percentage, fileFinalPath))
                }

                downloadCounter.completeDownloadSize.addAndGet(-file.totalSize)

                return@withContext
            }

            val sizeOnDisk = file.totalSize - neededChunks!!.sumOf { it.uncompressedLength }
            synchronized(depotDownloadCounter) {
                depotDownloadCounter.sizeDownloaded += sizeOnDisk
            }

            downloadCounter.completeDownloadSize.addAndGet(-sizeOnDisk)
        }

        val fileIsExecutable = file.flags.contains(EDepotFileFlag.Executable)
        if (fileIsExecutable &&
            (!fileDidExist || oldManifestFile == null || !oldManifestFile.flags.contains(EDepotFileFlag.Executable))
        ) {
            fileFinalPath.toFile().setExecutable(true)
        } else if (!fileIsExecutable &&
            oldManifestFile != null &&
            oldManifestFile.flags.contains(EDepotFileFlag.Executable)
        ) {
            fileFinalPath.toFile().setExecutable(false)
        }

        val fileStreamData = FileStreamData(
            fileHandle = null,
            fileLock = Mutex(),
            chunksToDownload = AtomicInteger(neededChunks!!.size)
        )

        neededChunks!!.forEach { chunk ->
            // Stop feeding the pipeline once the download has failed.
            tracker.session.throwIfFailed()

            tracker.chunkQueued()
            networkChunkChannel.send(
                NetworkChunkItem(
                    tracker = tracker,
                    downloadCounter = downloadCounter,
                    depotFilesData = depotFilesData,
                    fileStreamData = fileStreamData,
                    fileData = file,
                    chunk = chunk,
                    totalChunksForFile = neededChunks!!.size,
                )
            )
        }
    }

    private suspend fun downloadSteam3DepotFileChunk(
        tracker: DepotChunkTracker,
        downloadCounter: GlobalDownloadCounter,
        depotFilesData: DepotFilesData,
        file: FileData,
        fileStreamData: FileStreamData,
        chunk: ChunkData,
    ): DecompressItem = withContext(Dispatchers.IO) {
        ensureActive()

        val depot = depotFilesData.depotDownloadInfo
        val depotDownloadCounter = depotFilesData.depotCounter

        val chunkIdBytes = requireNotNull(chunk.chunkID) { "Chunk must have a ChunkID." }
        val chunkID = Strings.toHex(chunkIdBytes)

        var downloaded = 0
        val chunkBuffer = ByteArray(chunk.compressedLength)

        var failures = 0
        var lastError: Throwable? = null

        do {
            ensureActive()

            // Another chunk already failed this download; no point in fetching this one.
            tracker.session.throwIfFailed()

            if (failures >= MAX_DOWNLOAD_ATTEMPTS) break
            if (failures > 0) delay(retryDelayMillis(failures))

            var connection: Server? = null

            try {
                connection = cdnClientPool?.getConnection()
                    ?: throw IllegalStateException("ContentDownloader already closed")

                var cdnToken: String? = null

                val authTokenCallbackPromise = steam3!!.cdnAuthTokens[depot.depotId to connection.host]
                if (authTokenCallbackPromise != null) {
                    try {
                        val result = authTokenCallbackPromise.await()
                        cdnToken = result.token
                    } catch (e: Exception) {
                        ensureActive()
                        log.error("Failed to get CDN auth token: ${e.message}")
                    }
                }

                logger?.debug("Downloading chunk $chunkID from $connection with ${cdnClientPool!!.proxyServer ?: "no proxy"}")

                downloaded = cdnClientPool!!.cdnClient!!.downloadDepotChunk(
                    depotId = depot.depotId,
                    chunk = chunk,
                    server = connection,
                    destination = chunkBuffer,
                    depotKey = depot.depotKey,
                    proxyServer = cdnClientPool!!.proxyServer,
                    cdnAuthToken = cdnToken,
                )

                cdnClientPool!!.returnConnection(connection)

                break
            } catch (e: SteamKitWebRequestException) {
                // If the CDN returned 403, attempt to get a cdn auth if we didn't yet,
                // if auth task already exists, make sure it didn't complete yet, so that it gets awaited above
                if (e.statusCode == 403 &&
                    (
                        !steam3!!.cdnAuthTokens.containsKey(depot.depotId to connection!!.host) ||
                            steam3!!.cdnAuthTokens[depot.depotId to connection.host]?.isCompleted == false
                        )
                ) {
                    steam3!!.requestCDNAuthToken(depot.appId, depot.depotId, connection)

                    cdnClientPool!!.returnConnection(connection)

                    continue
                }

                cdnClientPool!!.skipConnection(connection)

                failures++
                lastError = e

                // Unauthorized || Forbidden
                if (e.statusCode == 401 || e.statusCode == 403) {
                    log.error("Encountered ${e.statusCode} for chunk $chunkID. Aborting.")
                    break
                }

                log.error("Encountered error downloading chunk $chunkID from $connection: ${e.statusCode} (${e.message})")
            } catch (e: Exception) {
                // A real cancellation stops here; a timeout reported as one is retried like any error.
                if (e is CancellationException) ensureActive()

                if (e is java.net.UnknownHostException || e.cause is java.net.UnknownHostException) {
                    cdnClientPool!!.returnBrokenConnection(connection)
                } else {
                    cdnClientPool!!.skipConnection(connection)
                }

                failures++
                lastError = e

                log.error("Encountered unexpected error downloading chunk $chunkID", e)
                if (!cdnClientPool!!.hasServers()) break
            }
        } while (downloaded == 0)

        // Giving up on one chunk fails the whole item: the pipeline reports it through the session,
        // which wakes downloadSteam3DepotFiles and ends in onDownloadFailed.
        if (downloaded == 0) {
            throw DepotDownloaderException(
                "Failed to download chunk $chunkID for depot ${depot.depotId} after $failures failed attempts",
                lastError
            )
        }

        // Return the decompress item for the next stage in the pipeline
        return@withContext DecompressItem(
            tracker = tracker,
            depot = depot,
            depotDownloadCounter = depotDownloadCounter,
            downloadCounter = downloadCounter,
            file = file,
            fileStreamData = fileStreamData,
            chunk = chunk,
            chunkBuffer = chunkBuffer,
        )
    }

    private fun testIsFileIncluded(filename: String): Boolean {
        if (!config.usingFileList) {
            return true
        }

        val normalizedFilename = filename.replace('\\', '/')

        if (normalizedFilename in config.filesToDownload) {
            return true
        }

        for (regex in config.filesToDownloadRegex) {
            if (regex.matches(normalizedFilename)) {
                return true
            }
        }

        return false
    }

    private fun finishDepotDownload(mainAppId: Int) {
        val appItem = processingItemsMap[mainAppId]
        if (appItem != null) {
            notifyListeners { it.onDownloadCompleted(appItem) }
        }
    }

    // endregion

    // region [REGION] Listener Operations

    fun addListener(listener: IDownloadListener) {
        listeners.add(listener)
    }

    fun removeListener(listener: IDownloadListener) {
        listeners.remove(listener)
    }

    private fun notifyListeners(action: (IDownloadListener) -> Unit) {
        listeners.forEach { action(it) }
    }

    // endregion

    // region [REGION] Queue Operations

    /**
     * Add a singular item of either [AppItem], [PubFileItem], or [UgcItem]
     */
    fun add(item: DownloadItem) {
        try {
            // The queue is unlimited, so this only fails once it has been closed.
            processingChannel.trySend(item).getOrThrow()
            notifyListeners { it.onItemAdded(item) }
        } catch (e: Exception) {
            log.error("Could not add item ${item.appId}", e)
            throw e
        }
    }

    /**
     * Add a list items of either [AppItem], [PubFileItem], or [UgcItem]
     */
    fun addAll(items: List<DownloadItem>) {
        items.forEach { add(it) }
    }

    /**
     * Signals that no more items will be added to the download queue.
     * After calling this, the downloader will complete once all queued items finish.
     *
     * This is called automatically by [close], but you can call it explicitly
     * if you want to wait for completion without closing the downloader.
     */
    fun finishAdding() {
        processingChannel.close()
    }

    // endregion

    private suspend fun processItems() = coroutineScope {
        if (useLanCache) {
            ClientLancache.detectLancacheServer()
        }

        if (ClientLancache.useLanCacheServer) {
            logger?.debug("Detected Lan-Cache server! Downloads will be directed through the LanCache.")
            if (maxDownloads == 8) {
                maxDownloads = 25
            }
        }

        // Launch the chunk processing pipeline using Flow
        chunkProcessingJob = scope.launch {
            createChunkProcessingFlow().collect()
        }

        try {
            for (item in processingChannel) {
                try {
                    ensureActive()

                    // Set configuration values
                    config = config.copy(
                        downloadManifestOnly = item.downloadManifestOnly,
                        installPath = item.installDirectory?.toPath(),
                        installToGameNameDirectory = item.installToGameNameDirectory,
                        verifyAll = item.verify,
                    )

                    processingItemsMap[item.appId] = item

                    when (item) {
                        is PubFileItem -> {
                            logger?.debug("Downloading PUB File for ${item.appId}")
                            notifyListeners { it.onDownloadStarted(item) }
                            downloadPubFile(item.appId, item.pubFile)
                        }

                        is UgcItem -> {
                            logger?.debug("Downloading UGC File for ${item.appId}")
                            notifyListeners { it.onDownloadStarted(item) }
                            downloadUGC(item.appId, item.ugcId)
                        }

                        is AppItem -> {
                            val branch = item.branch ?: DEFAULT_BRANCH
                            config = config.copy(betaPassword = item.branchPassword)

                            // Invalid arguments are thrown so they reach onDownloadFailed below.
                            if (!config.betaPassword.isNullOrBlank() && branch.isBlank()) {
                                throw IllegalArgumentException("Cannot specify 'branchPassword' when 'branch' is not specified.")
                            }

                            config = config.copy(downloadAllPlatforms = item.downloadAllPlatforms)
                            val os = item.os

                            if (config.downloadAllPlatforms && !os.isNullOrBlank()) {
                                throw IllegalArgumentException("Cannot specify 'os' when 'downloadAllPlatforms' is set.")
                            }

                            config = config.copy(downloadAllArchs = item.downloadAllArchs)
                            val arch = item.osArch

                            if (config.downloadAllArchs && !arch.isNullOrBlank()) {
                                throw IllegalArgumentException("Cannot specify 'osArch' when 'downloadAllArchs' is set.")
                            }

                            config = config.copy(downloadAllLanguages = item.downloadAllLanguages)
                            val language = item.language

                            if (config.downloadAllLanguages && !language.isNullOrBlank()) {
                                throw IllegalArgumentException("Cannot specify 'language' when 'downloadAllLanguages' is set.")
                            }

                            val depotManifestIds = mutableListOf<Pair<Int, Long>>()
                            val depotIdList = item.depot
                            val manifestIdList = item.manifest

                            if (manifestIdList.isNotEmpty()) {
                                if (depotIdList.size != manifestIdList.size) {
                                    throw IllegalArgumentException("'manifest' requires one id for every 'depot' specified.")
                                }
                                depotManifestIds.addAll(depotIdList.zip(manifestIdList))
                            } else {
                                depotManifestIds.addAll(depotIdList.map { it to INVALID_MANIFEST_ID })
                            }

                            logger?.debug("Downloading App for ${item.appId}")
                            notifyListeners { it.onDownloadStarted(item) }
                            downloadApp(
                                appId = item.appId,
                                depotManifestIds = depotManifestIds,
                                branch = branch,
                                os = os,
                                arch = arch,
                                language = language,
                                lv = item.lowViolence,
                                isUgc = false,
                            )
                        }
                    }
                } catch (e: Throwable) {
                    // Throwable, not Exception: e.g. a missing zstd/xz dependency is a NoClassDefFoundError.
                    log.error("Error downloading item ${item.appId}: ${e.message}", e)
                    notifyListeners { it.onDownloadFailed(item, e) }
                }
            }
        } finally {
            completionFuture.complete(null)
        }
    }

    private suspend fun processFileDecompress(item: DecompressItem): FileWriteItem = withContext(Dispatchers.Default) {
        // Throw the cancellation exception if requested so that this task is marked failed
        ensureActive()

        val depot = item.depot
        val chunk = item.chunk
        val chunkBuffer = item.chunkBuffer

        val decompressedBuffer = ByteArray(chunk.uncompressedLength)
        val written = DepotChunk.process(chunk, chunkBuffer, decompressedBuffer, depot.depotKey)

        return@withContext FileWriteItem(
            tracker = item.tracker,
            depot = depot,
            depotDownloadCounter = item.depotDownloadCounter,
            downloadCounter = item.downloadCounter,
            file = item.file,
            fileStreamData = item.fileStreamData,
            chunk = chunk,
            decompressed = written,
            decompressedBuffer = decompressedBuffer,
        )
    }

    private suspend fun processFileWrites(item: FileWriteItem): Unit = withContext(Dispatchers.IO) {
        // Throw the cancellation exception if requested so that this task is marked failed
        ensureActive()

        val depot = item.depot
        val depotDownloadCounter = item.depotDownloadCounter
        val downloadCounter = item.downloadCounter
        val file = item.file
        val fileStreamData = item.fileStreamData
        val chunk = item.chunk
        val written = item.decompressed
        val decompressedBuffer = item.decompressedBuffer

        try {
            fileStreamData.fileLock.lock()

            if (fileStreamData.fileHandle == null) {
                val fileFinalPath = depot.installDir / file.fileName
                fileStreamData.fileHandle = filesystem.openReadWrite(fileFinalPath)
            }

            fileStreamData.fileHandle!!.write(chunk.offset, decompressedBuffer, 0, written)
        } finally {
            fileStreamData.fileLock.unlock()
        }

        val remainingChunks = fileStreamData.chunksToDownload.decrementAndGet()
        if (remainingChunks == 0) {
            fileStreamData.fileHandle?.close()

            // File completed - notify with percentage
            val sizeDownloaded = synchronized(depotDownloadCounter) {
                depotDownloadCounter.sizeDownloaded += written.toLong()
                depotDownloadCounter.depotBytesCompressed += chunk.compressedLength
                depotDownloadCounter.depotBytesUncompressed += chunk.uncompressedLength
                depotDownloadCounter.sizeDownloaded
            }

            downloadCounter.totalBytesCompressed.addAndGet(chunk.compressedLength.toLong())
            downloadCounter.totalBytesUncompressed.addAndGet(chunk.uncompressedLength.toLong())

            val fileFinalPath = depot.installDir / file.fileName
            val depotPercentage = fraction(sizeDownloaded, depotDownloadCounter.completeDownloadSize)

            notifyListeners { listener ->
                listener.onFileCompleted(
                    depotId = depot.depotId,
                    fileName = fileFinalPath.toString(),
                    depotPercentComplete = depotPercentage
                )
            }

            logger?.debug("%.2f%% %s".format(depotPercentage, fileFinalPath))
        } else {
            // Update counters and notify on chunk completion
            val sizeDownloaded: Long
            val depotPercentage: Float
            val compressedBytes: Long
            val uncompressedBytes: Long

            synchronized(depotDownloadCounter) {
                depotDownloadCounter.sizeDownloaded += written.toLong()
                depotDownloadCounter.depotBytesCompressed += chunk.compressedLength
                depotDownloadCounter.depotBytesUncompressed += chunk.uncompressedLength

                sizeDownloaded = depotDownloadCounter.sizeDownloaded
                compressedBytes = depotDownloadCounter.depotBytesCompressed
                uncompressedBytes = depotDownloadCounter.depotBytesUncompressed
                depotPercentage = fraction(sizeDownloaded, depotDownloadCounter.completeDownloadSize)
            }

            downloadCounter.totalBytesCompressed.addAndGet(chunk.compressedLength.toLong())
            downloadCounter.totalBytesUncompressed.addAndGet(chunk.uncompressedLength.toLong())

            notifyListeners { listener ->
                listener.onChunkCompleted(
                    depotId = depot.depotId,
                    depotPercentComplete = depotPercentage,
                    compressedBytes = compressedBytes,
                    uncompressedBytes = uncompressedBytes
                )
            }
        }
    }

    /**
     * Returns a CompletableFuture that completes when all queued downloads finish.
     * @return CompletableFuture that completes when all downloads finish
     */
    fun getCompletion(): CompletableFuture<Void> = completionFuture

    /**
     * Blocks the current thread until all queued downloads complete.
     * Convenience method that calls `getCompletion().join()`.
     * @throws CompletionException if any download fails
     */
    fun awaitCompletion() {
        completionFuture.join()
    }

    override fun close() {
        processingChannel.close()
        networkChunkChannel.close()

        scope.cancel("DepotDownloader Closing")

        listeners.clear()

        steam3?.close()
        steam3 = null

        cdnClientPool?.close()
        cdnClientPool = null

        // Ours, derived from the SteamClient's HTTP client, which is left untouched.
        synchronized(httpClientLock) {
            ownHttpClient?.let { HttpClient.shutdown(it) }
            ownHttpClient = null
        }
    }
}
