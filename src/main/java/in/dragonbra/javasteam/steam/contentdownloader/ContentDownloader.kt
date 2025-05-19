package `in`.dragonbra.javasteam.steam.contentdownloader

import `in`.dragonbra.javasteam.enums.EDepotFileFlag
import `in`.dragonbra.javasteam.enums.EResult
import `in`.dragonbra.javasteam.steam.cdn.ClientPool
import `in`.dragonbra.javasteam.steam.cdn.Server
import `in`.dragonbra.javasteam.steam.handlers.steamapps.PICSProductInfo
import `in`.dragonbra.javasteam.steam.handlers.steamapps.PICSRequest
import `in`.dragonbra.javasteam.steam.handlers.steamapps.SteamApps
import `in`.dragonbra.javasteam.steam.handlers.steamapps.callback.PICSProductInfoCallback
import `in`.dragonbra.javasteam.steam.handlers.steamcontent.SteamContent
import `in`.dragonbra.javasteam.steam.steamclient.SteamClient
import `in`.dragonbra.javasteam.types.ChunkData
import `in`.dragonbra.javasteam.types.DepotManifest
import `in`.dragonbra.javasteam.types.FileData
import `in`.dragonbra.javasteam.types.KeyValue
import `in`.dragonbra.javasteam.util.SteamKitWebRequestException
import `in`.dragonbra.javasteam.util.Strings
import `in`.dragonbra.javasteam.util.Utils
import `in`.dragonbra.javasteam.util.compat.readNBytesCompat
import `in`.dragonbra.javasteam.util.log.LogManager
import `in`.dragonbra.javasteam.util.log.Logger
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.future.future
import kotlinx.coroutines.isActive
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.RandomAccessFile
import java.nio.ByteBuffer
import java.nio.file.Paths
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentLinkedQueue

@Suppress("unused", "SpellCheckingInspection")
class ContentDownloader(val steamClient: SteamClient) {

    companion object {
        private const val HTTP_UNAUTHORIZED = 401
        private const val HTTP_FORBIDDEN = 403
        private const val HTTP_NOT_FOUND = 404
        private const val SERVICE_UNAVAILABLE = 503

        internal const val INVALID_APP_ID = Int.MAX_VALUE
        internal const val INVALID_MANIFEST_ID = Long.MAX_VALUE

        private val logger: Logger = LogManager.getLogger(ContentDownloader::class.java)
    }

    private val defaultScope = CoroutineScope(Dispatchers.IO)

    private fun requestDepotKey(
        appId: Int,
        depotId: Int,
        parentScope: CoroutineScope,
    ): Deferred<Pair<EResult, ByteArray?>> = parentScope.async {
        val steamApps = steamClient.getHandler(SteamApps::class.java)
        val callback = steamApps?.getDepotDecryptionKey(depotId, appId)?.await()

        return@async Pair(callback?.result ?: EResult.Fail, callback?.depotKey)
    }

    private fun getDepotManifestId(
        app: PICSProductInfo,
        depotId: Int,
        branchId: String,
        parentScope: CoroutineScope,
    ): Deferred<Pair<Int, Long>> = parentScope.async {
        val depot = app.keyValues["depots"][depotId.toString()]
        if (depot == KeyValue.INVALID) {
            logger.error("Could not find depot $depotId of ${app.id}")
            return@async Pair(app.id, INVALID_MANIFEST_ID)
        }

        val manifest = depot["manifests"][branchId]
        if (manifest != KeyValue.INVALID) {
            return@async Pair(app.id, manifest["gid"].asLong())
        }

        val depotFromApp = depot["depotfromapp"].asInteger(INVALID_APP_ID)
        if (depotFromApp == app.id || depotFromApp == INVALID_APP_ID) {
            logger.error("Failed to find manifest of app ${app.id} within depot $depotId on branch $branchId")
            return@async Pair(app.id, INVALID_MANIFEST_ID)
        }

        val innerApp = getAppInfo(depotFromApp, parentScope).await()
        if (innerApp == null) {
            logger.error("Failed to find manifest of app ${app.id} within depot $depotId on branch $branchId")
            return@async Pair(app.id, INVALID_MANIFEST_ID)
        }

        return@async getDepotManifestId(innerApp, depotId, branchId, parentScope).await()
    }

    private fun getAppDirName(app: PICSProductInfo): String {
        val installDirKeyValue = app.keyValues["config"]["installdir"]

        return if (installDirKeyValue != KeyValue.INVALID) installDirKeyValue.value else app.id.toString()
    }

    private fun getAppInfo(
        appId: Int,
        parentScope: CoroutineScope,
    ): Deferred<PICSProductInfo?> = parentScope.async {
        val steamApps = steamClient.getHandler(SteamApps::class.java)
        val callback = steamApps?.picsGetProductInfo(PICSRequest(appId))?.await()
        val apps = callback?.results?.flatMap { (it as PICSProductInfoCallback).apps.values }

        if (apps.isNullOrEmpty()) {
            logger.error("Received empty apps list in PICSProductInfo response for $appId")
            return@async null
        }

        if (apps.size > 1) {
            logger.debug("Received ${apps.size} apps from PICSProductInfo for $appId, using first result")
        }

        return@async apps.first()
    }

    /**
     * Kotlin coroutines version
     */
    fun downloadApp(
        appId: Int,
        depotId: Int,
        installPath: String,
        stagingPath: String,
        branch: String = "public",
        maxDownloads: Int = 8,
        onDownloadProgress: ((Float) -> Unit)? = null,
        parentScope: CoroutineScope = defaultScope,
    ): Deferred<Boolean> = parentScope.async {
        downloadAppInternal(
            appId = appId,
            depotId = depotId,
            installPath = installPath,
            stagingPath = stagingPath,
            branch = branch,
            maxDownloads = maxDownloads,
            onDownloadProgress = onDownloadProgress,
            scope = parentScope
        )
    }

    /**
     * Java-friendly version that returns a CompletableFuture
     */
    @JvmOverloads
    fun downloadApp(
        appId: Int,
        depotId: Int,
        installPath: String,
        stagingPath: String,
        branch: String = "public",
        maxDownloads: Int = 8,
        progressCallback: ProgressCallback? = null,
    ): CompletableFuture<Boolean> = defaultScope.future {
        return@future downloadAppInternal(
            appId = appId,
            depotId = depotId,
            installPath = installPath,
            stagingPath = stagingPath,
            branch = branch,
            maxDownloads = maxDownloads,
            onDownloadProgress = progressCallback?.let { callback -> { progress -> callback.onProgress(progress) } },
            scope = defaultScope
        )
    }

    private suspend fun downloadAppInternal(
        appId: Int,
        depotId: Int,
        installPath: String,
        stagingPath: String,
        branch: String = "public",
        maxDownloads: Int = 8,
        onDownloadProgress: ((Float) -> Unit)? = null,
        scope: CoroutineScope,
    ): Boolean {
        if (!scope.isActive) {
            logger.error("App $appId was not completely downloaded. Operation was canceled.")
            return false
        }

        val cdnPool = ClientPool(steamClient, appId, scope)

        val shiftedAppId: Int
        val manifestId: Long
        val appInfo = getAppInfo(appId, scope).await()

        if (appInfo == null) {
            logger.error("Could not retrieve PICSProductInfo of $appId")
            return false
        }

        getDepotManifestId(appInfo, depotId, branch, scope).await().apply {
            shiftedAppId = first
            manifestId = second
        }

        val depotKeyResult = requestDepotKey(shiftedAppId, depotId, scope).await()

        if (depotKeyResult.first != EResult.OK || depotKeyResult.second == null) {
            logger.error("Depot key request for $appId failed with result ${depotKeyResult.first}")
            return false
        }

        val depotKey = depotKeyResult.second!!

        var newProtoManifest = steamClient.configuration.depotManifestProvider.fetchManifest(depotId, manifestId)
        var oldProtoManifest = steamClient.configuration.depotManifestProvider.fetchLatestManifest(depotId)

        if (oldProtoManifest?.manifestGID == manifestId) {
            oldProtoManifest = null
        }

        // In case we have an early exit, this will force equiv of verifyall next run.
        steamClient.configuration.depotManifestProvider.setLatestManifestId(depotId, INVALID_MANIFEST_ID)

        try {
            if (newProtoManifest == null) {
                newProtoManifest =
                    downloadFilesManifestOf(shiftedAppId, depotId, manifestId, branch, depotKey, cdnPool, scope).await()
            } else {
                logger.debug("Already have manifest $manifestId for depot $depotId.")
            }

            if (newProtoManifest == null) {
                logger.error("Failed to retrieve files manifest for app: $shiftedAppId depot: $depotId manifest: $manifestId branch: $branch")
                return false
            }

            if (!scope.isActive) {
                return false
            }

            val downloadCounter = GlobalDownloadCounter()
            val installDir = Paths.get(installPath, getAppDirName(appInfo)).toString()
            val stagingDir = Paths.get(stagingPath, getAppDirName(appInfo)).toString()
            val depotFileData = DepotFilesData(
                depotDownloadInfo = DepotDownloadInfo(depotId, shiftedAppId, manifestId, branch, installDir, depotKey),
                depotCounter = DepotDownloadCounter(
                    completeDownloadSize = newProtoManifest.totalUncompressedSize
                ),
                stagingDir = stagingDir,
                manifest = newProtoManifest,
                previousManifest = oldProtoManifest
            )

            downloadDepotFiles(cdnPool, downloadCounter, depotFileData, maxDownloads, onDownloadProgress, scope).await()

            steamClient.configuration.depotManifestProvider.setLatestManifestId(depotId, manifestId)

            cdnPool.shutdown()

            // delete the staging directory of this app
            File(stagingDir).deleteRecursively()

            logger.debug(
                "Depot $depotId - Downloaded ${depotFileData.depotCounter.depotBytesCompressed} " +
                    "bytes (${depotFileData.depotCounter.depotBytesUncompressed} bytes uncompressed)"
            )

            return true
        } catch (e: CancellationException) {
            logger.error("App $appId was not completely downloaded. Operation was canceled.")

            return false
        } catch (e: Exception) {
            logger.error("Error occurred while downloading app $shiftedAppId", e)

            return false
        }
    }

    private fun downloadDepotFiles(
        cdnPool: ClientPool,
        downloadCounter: GlobalDownloadCounter,
        depotFilesData: DepotFilesData,
        maxDownloads: Int,
        onDownloadProgress: ((Float) -> Unit)? = null,
        parentScope: CoroutineScope,
    ) = parentScope.async {
        if (!parentScope.isActive) {
            return@async
        }

        depotFilesData.manifest.files.forEach { file ->
            val fileFinalPath = Paths.get(depotFilesData.depotDownloadInfo.installDir, file.fileName).toString()
            val fileStagingPath = Paths.get(depotFilesData.stagingDir, file.fileName).toString()

            if (file.flags.contains(EDepotFileFlag.Directory)) {
                File(fileFinalPath).mkdirs()
                File(fileStagingPath).mkdirs()
            } else {
                // Some manifests don't explicitly include all necessary directories
                File(fileFinalPath).parentFile.mkdirs()
                File(fileStagingPath).parentFile.mkdirs()
            }
        }

        logger.debug("Downloading depot ${depotFilesData.depotDownloadInfo.depotId}")

        val files = depotFilesData.manifest.files.filter { !it.flags.contains(EDepotFileFlag.Directory) }.toTypedArray()
        val networkChunkQueue = ConcurrentLinkedQueue<Triple<FileStreamData, FileData, ChunkData>>()

        val downloadSemaphore = Semaphore(maxDownloads)
        files.map { file ->
            async {
                downloadSemaphore.withPermit {
                    downloadDepotFile(depotFilesData, file, networkChunkQueue, onDownloadProgress, parentScope).await()
                }
            }
        }.awaitAll()

        networkChunkQueue.map { (fileStreamData, fileData, chunk) ->
            async {
                downloadSemaphore.withPermit {
                    downloadSteam3DepotFileChunk(
                        cdnPool = cdnPool,
                        downloadCounter = downloadCounter,
                        depotFilesData = depotFilesData,
                        file = fileData,
                        fileStreamData = fileStreamData,
                        chunk = chunk,
                        onDownloadProgress = onDownloadProgress,
                        parentScope = parentScope
                    ).await()
                }
            }
        }.awaitAll()

        // Check for deleted files if updating the depot.
        depotFilesData.previousManifest?.apply {
            val previousFilteredFiles = files.asSequence().map { it.fileName }.toMutableSet()

            // Of the list of files in the previous manifest, remove any file names that exist in the current set of all file names
            previousFilteredFiles.removeAll(depotFilesData.manifest.files.map { it.fileName }.toSet())

            for (existingFileName in previousFilteredFiles) {
                val fileFinalPath = Paths.get(depotFilesData.depotDownloadInfo.installDir, existingFileName).toString()

                if (!File(fileFinalPath).exists()) {
                    continue
                }

                File(fileFinalPath).delete()
                logger.debug("Deleted $fileFinalPath")
            }
        }
    }

    private fun downloadDepotFile(
        depotFilesData: DepotFilesData,
        file: FileData,
        networkChunkQueue: ConcurrentLinkedQueue<Triple<FileStreamData, FileData, ChunkData>>,
        onDownloadProgress: ((Float) -> Unit)? = null,
        parentScope: CoroutineScope,
    ) = parentScope.async {
        if (!isActive) {
            return@async
        }

        val depotDownloadCounter = depotFilesData.depotCounter
        val oldManifestFile = depotFilesData.previousManifest?.files?.find { it.fileName == file.fileName }

        val fileFinalPath = Paths.get(depotFilesData.depotDownloadInfo.installDir, file.fileName).toString()
        val fileStagingPath = Paths.get(depotFilesData.stagingDir, file.fileName).toString()

        // This may still exist if the previous run exited before cleanup
        File(fileStagingPath).takeIf { it.exists() }?.delete()

        val neededChunks: MutableList<ChunkData>
        val fi = File(fileFinalPath)
        val fileDidExist = fi.exists()

        if (!fileDidExist) {
            // create new file. need all chunks
            FileOutputStream(fileFinalPath).use { fs ->
                fs.channel.truncate(file.totalSize)
            }

            neededChunks = file.chunks.toMutableList()
        } else {
            // open existing
            if (oldManifestFile != null) {
                neededChunks = mutableListOf()

                val hashMatches = oldManifestFile.fileHash.contentEquals(file.fileHash)
                if (!hashMatches) {
                    logger.debug("Validating $fileFinalPath")

                    val matchingChunks = mutableListOf<ChunkMatch>()

                    for (chunk in file.chunks) {
                        val oldChunk = oldManifestFile.chunks.find { it.chunkID.contentEquals(chunk.chunkID) }
                        if (oldChunk != null) {
                            matchingChunks.add(ChunkMatch(oldChunk, chunk))
                        } else {
                            neededChunks.add(chunk)
                        }
                    }

                    val orderedChunks = matchingChunks.sortedBy { it.oldChunk.offset }

                    val copyChunks = mutableListOf<ChunkMatch>()

                    FileInputStream(fileFinalPath).use { fsOld ->
                        for (match in orderedChunks) {
                            fsOld.channel.position(match.oldChunk.offset)

                            val tmp = ByteArray(match.oldChunk.uncompressedLength)
                            fsOld.readNBytesCompat(tmp, 0, tmp.size)

                            val adler = Utils.adlerHash(tmp)
                            if (adler != match.oldChunk.checksum) {
                                neededChunks.add(match.newChunk)
                            } else {
                                copyChunks.add(match)
                            }
                        }
                    }

                    if (neededChunks.isNotEmpty()) {
                        File(fileFinalPath).renameTo(File(fileStagingPath))

                        FileInputStream(fileStagingPath).use { fsOld ->
                            FileOutputStream(fileFinalPath).use { fs ->
                                fs.channel.truncate(file.totalSize)

                                for (match in copyChunks) {
                                    fsOld.channel.position(match.oldChunk.offset)

                                    val tmp = ByteArray(match.oldChunk.uncompressedLength)
                                    fsOld.readNBytesCompat(tmp, 0, tmp.size)

                                    fs.channel.position(match.newChunk.offset)
                                    fs.write(tmp)
                                }
                            }
                        }

                        File(fileStagingPath).delete()
                    }
                }
            } else {
                // No old manifest or file not in old manifest. We must validate.
                RandomAccessFile(fileFinalPath, "rw").use { fs ->
                    if (fi.length() != file.totalSize) {
                        fs.channel.truncate(file.totalSize)
                    }

                    logger.debug("Validating $fileFinalPath")
                    neededChunks = Utils.validateSteam3FileChecksums(
                        fs,
                        file.chunks.sortedBy { it.offset }.toTypedArray()
                    )
                }
            }

            if (neededChunks.isEmpty()) {
                synchronized(depotDownloadCounter) {
                    depotDownloadCounter.sizeDownloaded += file.totalSize
                }

                onDownloadProgress?.apply {
                    val totalPercent =
                        depotFilesData.depotCounter.sizeDownloaded.toFloat() / depotFilesData.depotCounter.completeDownloadSize
                    this(totalPercent)
                }

                return@async
            }

            val sizeOnDisk = file.totalSize - neededChunks.sumOf { it.uncompressedLength.toLong() }
            synchronized(depotDownloadCounter) {
                depotDownloadCounter.sizeDownloaded += sizeOnDisk
            }

            onDownloadProgress?.apply {
                val totalPercent =
                    depotFilesData.depotCounter.sizeDownloaded.toFloat() / depotFilesData.depotCounter.completeDownloadSize
                this(totalPercent)
            }
        }

        val fileIsExecutable = file.flags.contains(EDepotFileFlag.Executable)
        if (fileIsExecutable &&
            (!fileDidExist || oldManifestFile == null || !oldManifestFile.flags.contains(EDepotFileFlag.Executable))
        ) {
            File(fileFinalPath).setExecutable(true)
        } else if (!fileIsExecutable && oldManifestFile != null && oldManifestFile.flags.contains(EDepotFileFlag.Executable)) {
            File(fileFinalPath).setExecutable(false)
        }

        val fileStreamData = FileStreamData(
            fileStream = null,
            fileLock = Semaphore(1),
            chunksToDownload = neededChunks.size
        )

        for (chunk in neededChunks) {
            networkChunkQueue.add(Triple(fileStreamData, file, chunk))
        }
    }

    private fun downloadSteam3DepotFileChunk(
        cdnPool: ClientPool,
        downloadCounter: GlobalDownloadCounter,
        depotFilesData: DepotFilesData,
        file: FileData,
        fileStreamData: FileStreamData,
        chunk: ChunkData,
        onDownloadProgress: ((Float) -> Unit)? = null,
        parentScope: CoroutineScope,
    ) = parentScope.async {
        if (!isActive) {
            return@async
        }

        val depot = depotFilesData.depotDownloadInfo
        val depotDownloadCounter = depotFilesData.depotCounter

        val chunkID = Strings.toHex(chunk.chunkID)

        var outputChunkData = ByteArray(chunk.uncompressedLength)
        var writtenBytes = 0

        do {
            var connection: Server? = null

            try {
                connection = cdnPool.getConnection().await()

                outputChunkData = ByteArray(chunk.uncompressedLength)
                writtenBytes = cdnPool.cdnClient.downloadDepotChunk(
                    depotId = depot.depotId,
                    chunk = chunk,
                    server = connection!!,
                    destination = outputChunkData,
                    depotKey = depot.depotKey,
                    proxyServer = cdnPool.proxyServer
                )

                cdnPool.returnConnection(connection)
            } catch (e: SteamKitWebRequestException) {
                cdnPool.returnBrokenConnection(connection)

                when (e.statusCode) {
                    HTTP_UNAUTHORIZED, HTTP_FORBIDDEN -> {
                        logger.error("Encountered ${e.statusCode} for chunk $chunkID. Aborting.")
                        break
                    }

                    else -> logger.error("Encountered error downloading chunk $chunkID: ${e.statusCode}")
                }
            } catch (e: NoClassDefFoundError) {
                // Zstd is a 'compileOnly' dependency.
                throw CancellationException(e.message)
            } catch (e: Exception) {
                cdnPool.returnBrokenConnection(connection)

                logger.error("Encountered unexpected error downloading chunk $chunkID", e)
            }
        } while (isActive && writtenBytes <= 0)

        if (writtenBytes <= 0) {
            logger.error("Failed to find any server with chunk $chunkID for depot ${depot.depotId}. Aborting.")
            throw CancellationException("Failed to download chunk")
        }

        try {
            fileStreamData.fileLock.acquire()

            if (fileStreamData.fileStream == null) {
                val fileFinalPath = Paths.get(depot.installDir, file.fileName).toString()
                val randomAccessFile = RandomAccessFile(fileFinalPath, "rw")
                fileStreamData.fileStream = randomAccessFile.channel
            }

            fileStreamData.fileStream?.position(chunk.offset)
            fileStreamData.fileStream?.write(ByteBuffer.wrap(outputChunkData, 0, writtenBytes))
        } finally {
            fileStreamData.fileLock.release()
        }

        val remainingChunks = synchronized(fileStreamData) {
            --fileStreamData.chunksToDownload
        }
        if (remainingChunks <= 0) {
            fileStreamData.fileStream?.close()
        }

        var sizeDownloaded: Long
        synchronized(depotDownloadCounter) {
            sizeDownloaded = depotDownloadCounter.sizeDownloaded + outputChunkData.size
            depotDownloadCounter.sizeDownloaded = sizeDownloaded
            depotDownloadCounter.depotBytesCompressed += chunk.compressedLength
            depotDownloadCounter.depotBytesUncompressed += chunk.uncompressedLength
        }

        synchronized(downloadCounter) {
            downloadCounter.totalBytesCompressed += chunk.compressedLength
            downloadCounter.totalBytesUncompressed += chunk.uncompressedLength
        }

        onDownloadProgress?.invoke(
            depotFilesData.depotCounter.sizeDownloaded.toFloat() / depotFilesData.depotCounter.completeDownloadSize
        )
    }

    private fun downloadFilesManifestOf(
        appId: Int,
        depotId: Int,
        manifestId: Long,
        branch: String,
        depotKey: ByteArray,
        cdnPool: ClientPool,
        parentScope: CoroutineScope,
    ): Deferred<DepotManifest?> = parentScope.async {
        if (!isActive) {
            return@async null
        }

        var depotManifest: DepotManifest? = null
        var manifestRequestCode = 0UL
        var manifestRequestCodeExpiration = Instant.MIN

        do {
            var connection: Server? = null

            try {
                connection = cdnPool.getConnection().await()

                if (connection == null) continue

                val now = Instant.now()

                // In order to download this manifest, we need the current manifest request code
                // The manifest request code is only valid for a specific period of time
                if (manifestRequestCode == 0UL || now >= manifestRequestCodeExpiration) {
                    val steamContent = steamClient.getHandler(SteamContent::class.java)!!

                    manifestRequestCode = steamContent.getManifestRequestCode(
                        depotId = depotId,
                        appId = appId,
                        manifestId = manifestId,
                        branch = branch,
                        parentScope = parentScope
                    ).await()

                    // This code will hopefully be valid for one period following the issuing period
                    manifestRequestCodeExpiration = now.plus(5, ChronoUnit.MINUTES)

                    // If we could not get the manifest code, this is a fatal error
                    if (manifestRequestCode == 0UL) {
                        throw CancellationException("No manifest request code was returned for manifest $manifestId in depot $depotId")
                    }
                }

                depotManifest = cdnPool.cdnClient.downloadManifest(
                    depotId = depotId,
                    manifestId = manifestId,
                    manifestRequestCode = manifestRequestCode,
                    server = connection,
                    depotKey = depotKey,
                    proxyServer = cdnPool.proxyServer
                )

                cdnPool.returnConnection(connection)
            } catch (e: CancellationException) {
                logger.error("Connection timeout downloading depot manifest $depotId $manifestId")

                return@async null
            } catch (e: SteamKitWebRequestException) {
                cdnPool.returnBrokenConnection(connection)

                val statusName = when (e.statusCode) {
                    HTTP_UNAUTHORIZED -> HTTP_UNAUTHORIZED::class.java.name
                    HTTP_FORBIDDEN -> HTTP_FORBIDDEN::class.java.name
                    HTTP_NOT_FOUND -> HTTP_NOT_FOUND::class.java.name
                    SERVICE_UNAVAILABLE -> SERVICE_UNAVAILABLE::class.java.name
                    else -> null
                }

                logger.error(
                    "Downloading of manifest $manifestId failed for depot $depotId with " +
                        if (statusName != null) {
                            "response of $statusName(${e.statusCode})"
                        } else {
                            "status code of ${e.statusCode}"
                        }
                )

                return@async null
            } catch (e: Exception) {
                cdnPool.returnBrokenConnection(connection)

                logger.error("Encountered error downloading manifest for depot $depotId $manifestId", e)

                return@async null
            }
        } while (isActive && depotManifest == null)

        if (depotManifest == null) {
            throw CancellationException("Unable to download manifest $manifestId for depot $depotId")
        }

        val newProtoManifest = depotManifest
        steamClient.configuration.depotManifestProvider.updateManifest(newProtoManifest)

        return@async newProtoManifest
    }
}
