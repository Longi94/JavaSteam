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
import `in`.dragonbra.javasteam.util.Utils
import `in`.dragonbra.javasteam.util.log.LogManager
import `in`.dragonbra.javasteam.util.log.Logger
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.*
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.RandomAccessFile
import java.nio.ByteBuffer
import java.nio.file.Paths
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.concurrent.ConcurrentLinkedQueue

class ContentDownloader(val steamClient: SteamClient) {
    companion object {
        private const val HTTP_UNAUTHORIZED = 401
        private const val HTTP_FORBIDDEN = 403
        private const val HTTP_NOT_FOUND = 404
        internal const val INVALID_APP_ID = Int.MAX_VALUE
        internal const val INVALID_MANIFEST_ID = Long.MAX_VALUE
        private val logger: Logger = LogManager.getLogger(ContentDownloader::class.java)
    }

    private fun requestDepotKey(
        appId: Int,
        depotId: Int,
        parentScope: CoroutineScope
    ): Deferred<Pair<EResult, ByteArray?>> = parentScope.async {
        val steamApps = steamClient.getHandler(SteamApps::class.java)
        val callback = steamApps?.getDepotDecryptionKey(depotId, appId)?.toDeferred()?.await()
        return@async Pair(callback?.result ?: EResult.Fail, callback?.depotKey ?: null)
    }
    private fun getDepotManifestId(
        app: PICSProductInfo,
        depotId: Int,
        branchId: String,
        parentScope: CoroutineScope
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
        parentScope: CoroutineScope
    ): Deferred<PICSProductInfo?> = parentScope.async {
        val steamApps = steamClient.getHandler(SteamApps::class.java)
        val callback = steamApps?.picsGetProductInfo(PICSRequest(appId))?.toDeferred()?.await()
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
    fun downloadApp(
        appId: Int,
        depotId: Int,
        installPath: String,
        stagingPath: String,
        branch: String = "public",
        maxDownloads: Int = 8,
        onDownloadProgress: ((Float) -> Unit)? = null,
        parentScope: CoroutineScope
    ): Deferred<Boolean> = parentScope.async {
        if (!isActive) {
            logger.error("App $appId was not completely downloaded. Operation was canceled.")
            return@async false
        }

        val cdnPool = ClientPool(steamClient, appId, parentScope)

        val shiftedAppId: Int
        val manifestId: Long
        val appInfo = getAppInfo(appId, parentScope).await()
        if (appInfo == null) {
            logger.error("Could not retrieve PICSProductInfo of $appId")
            return@async false
        }
        getDepotManifestId(appInfo, depotId, branch, parentScope).await().apply {
            shiftedAppId = first
            manifestId = second
        }
        val depotKeyResult = requestDepotKey(shiftedAppId, depotId, parentScope).await()
        if (depotKeyResult.first != EResult.OK || depotKeyResult.second == null)
            return@async false
        val depotKey = depotKeyResult.second!!

        var newProtoManifest = steamClient.configuration.depotManifestProvider.fetchManifest(depotId, manifestId)
        var oldProtoManifest = steamClient.configuration.depotManifestProvider.fetchLatestManifest(depotId)
        if (oldProtoManifest?.first?.manifestGID == manifestId)
            oldProtoManifest = null

        // In case we have an early exit, this will force equiv of verifyall next run.
        steamClient.configuration.depotManifestProvider.setLatestManifestId(depotId, INVALID_MANIFEST_ID)

        try {
//            coroutineContext.ensureActive()
//            cdnPool.exhaustedJob = coroutineContext.job
            if (newProtoManifest == null) {
                newProtoManifest = getFilesManifestOf(shiftedAppId, depotId, manifestId, branch, depotKey, cdnPool, parentScope).await()
            }

            if (newProtoManifest == null) {
                logger.error("Failed to retrieve files manifest for app: $shiftedAppId depot: $depotId manifest: $manifestId branch: $branch")
                return@async false
            }
            if (!coroutineContext.isActive) {
                return@async false
            }

            val downloadCounter = GlobalDownloadCounter()
            val installDir = Paths.get(installPath, getAppDirName(appInfo)).toString()
            val stagingDir = Paths.get(stagingPath, getAppDirName(appInfo)).toString()
            val depotFileData = DepotFilesData(
                depotDownloadInfo = DepotDownloadInfo(depotId, shiftedAppId, manifestId, branch, installDir, depotKey),
                depotCounter = DepotDownloadCounter(
                    completeDownloadSize = newProtoManifest.first.totalUncompressedSize
                ),
                stagingDir = stagingDir,
                manifest = newProtoManifest.first,
                previousManifest = oldProtoManifest?.first
            )
            downloadDepotFiles(cdnPool, downloadCounter, depotFileData, maxDownloads, onDownloadProgress, parentScope).await()
            steamClient.configuration.depotManifestProvider.setLatestManifestId(depotId, manifestId)
            cdnPool.shutdown()

            logger.debug("Depot $depotId - Downloaded ${depotFileData.depotCounter.depotBytesCompressed} bytes (${depotFileData.depotCounter.depotBytesUncompressed} bytes uncompressed)")
            return@async true
//        } catch (e: CancellationException) {
//            logger.error("App $appId was not completely downloaded. Operation was canceled.")
//            return@async false
        } catch (e: Exception) {
            logger.error("Error occurred while downloading app $shiftedAppId: ${e.message}")
            e.printStackTrace()
            return@async false
        }
    }

    private fun downloadDepotFiles(
        cdnPool: ClientPool,
        downloadCounter: GlobalDownloadCounter,
        depotFilesData: DepotFilesData,
        maxDownloads: Int,
        onDownloadProgress: ((Float) -> Unit)? = null,
        parentScope: CoroutineScope
    ) = parentScope.async {
        if (!parentScope.isActive)
            return@async

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

//        logger.debug("Downloading depot ${depotFilesData.depotDownloadInfo.depotId}")

        val files = depotFilesData.manifest.files.filter { !it.flags.contains(EDepotFileFlag.Directory) }.toTypedArray()
//        logger.debug("Filtered files in manifest list")
        val networkChunkQueue = ConcurrentLinkedQueue<Triple<FileStreamData, FileData, ChunkData>>()
//        logger.debug("Prepared network chunk queue")

        val downloadSemaphore = Semaphore(maxDownloads)
//        logger.debug("Prepared download semaphore")
        files.map { file ->
            async {
                downloadSemaphore.withPermit {
                    downloadDepotFile(depotFilesData, file, networkChunkQueue, onDownloadProgress, parentScope).await()
                }
            }
        }.awaitAll()
//        logger.debug("Downloaded depot files")

//        networkChunkQueue.forEach { (fileStreamData, fileData, chunk) ->
//            // Process one at a time, completely
//            downloadSteam3DepotFileChunk(
//                cdnPool,
//                downloadCounter,
//                depotFilesData,
//                fileData,
//                fileStreamData,
//                chunk,
//                onDownloadProgress,
//                parentScope
//            ).await()
//        }
        networkChunkQueue.map { (fileStreamData, fileData, chunk) ->
            async {
                downloadSemaphore.withPermit {
                    downloadSteam3DepotFileChunk(cdnPool, downloadCounter, depotFilesData,
                        fileData, fileStreamData, chunk, onDownloadProgress, parentScope).await()
                }
            }
        }.awaitAll()
//        logger.debug("Downloaded depot file chunks")

        // Check for deleted files if updating the depot.
        depotFilesData.previousManifest?.let { previousManifest ->
            val previousFilteredFiles = previousManifest.files.asSequence().map { it.fileName }.toMutableSet()

            // Of the list of files in the previous manifest, remove any file names that exist in the current set of all file names
            previousFilteredFiles.removeAll(depotFilesData.manifest.files.map { it.fileName }.toSet())

            for (existingFileName in previousFilteredFiles) {
                val fileFinalPath = Paths.get(depotFilesData.depotDownloadInfo.installDir, existingFileName).toString()

                if (!File(fileFinalPath).exists()) continue

                File(fileFinalPath).delete()
//                logger.debug("Deleted $fileFinalPath")
            }
        }
//        logger.debug("Removed files exclusive to previous manifest")
    }

    private fun downloadDepotFile(
        depotFilesData: DepotFilesData,
        file: FileData,
        networkChunkQueue: ConcurrentLinkedQueue<Triple<FileStreamData, FileData, ChunkData>>,
        onDownloadProgress: ((Float) -> Unit)? = null,
        parentScope: CoroutineScope
    ) = parentScope.async {
        if (!isActive)
            return@async

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
                            fsOld.readNBytes(tmp, 0, tmp.size)

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
                                    fsOld.readNBytes(tmp, 0, tmp.size)

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

                FileInputStream(fileFinalPath).use { fs ->
//                    if (fi.length() != file.totalSize) {
//                        fs.channel.truncate(file.totalSize)
//                    }

                    logger.debug("Validating $fileFinalPath")
                    neededChunks = Utils.validateSteam3FileChecksums(fs, file.chunks.sortedBy { it.offset }.toTypedArray())
                }
            }

            if (neededChunks.isEmpty()) {
                synchronized(depotDownloadCounter) {
                    depotDownloadCounter.sizeDownloaded += file.totalSize
                }

                onDownloadProgress?.apply {
                    val totalPercent = depotFilesData.depotCounter.sizeDownloaded.toFloat() / depotFilesData.depotCounter.completeDownloadSize
                    this(totalPercent)
                }

                return@async
            }

            val sizeOnDisk = file.totalSize - neededChunks.sumOf { it.uncompressedLength.toLong() }
            synchronized(depotDownloadCounter) {
                depotDownloadCounter.sizeDownloaded += sizeOnDisk
            }
            onDownloadProgress?.apply {
                val totalPercent = depotFilesData.depotCounter.sizeDownloaded.toFloat() / depotFilesData.depotCounter.completeDownloadSize
                this(totalPercent)
            }
        }

        val fileIsExecutable = file.flags.contains(EDepotFileFlag.Executable)
        if (fileIsExecutable && (!fileDidExist || oldManifestFile == null || !oldManifestFile.flags.contains(EDepotFileFlag.Executable))) {
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
        parentScope: CoroutineScope
    ) = parentScope.async {
        if (!isActive)
            return@async

        val depot = depotFilesData.depotDownloadInfo
        val depotDownloadCounter = depotFilesData.depotCounter

        val chunkID = Utils.encodeHexString(chunk.chunkID)

        val chunkInfo = ChunkData(chunk)

        var outputChunkData = ByteArray(chunkInfo.uncompressedLength)
        var writtenBytes = 0

        do {
//            if (!isActive)
//                break

            var connection: Server? = null

            try {
                connection = cdnPool.getConnection().await()

//                logger.debug("Downloading chunk $chunkID from $connection through ${cdnPool.proxyServer ?: "no proxy"}")
                outputChunkData = ByteArray(chunkInfo.uncompressedLength)
                writtenBytes = cdnPool.cdnClient.downloadDepotChunk(
                    depotId = depot.depotId,
                    chunk = chunkInfo,
                    server = connection!!,
                    destination = outputChunkData,
                    depotKey = depot.depotKey,
                    proxyServer = cdnPool.proxyServer
                )

                cdnPool.returnConnection(connection)
//            } catch (e: TaskCanceledException) {
//                println("Connection timeout downloading chunk $chunkID")
            } catch (e: SteamKitWebRequestException) {
                cdnPool.returnBrokenConnection(connection)

                when (e.statusCode) {
                    HTTP_UNAUTHORIZED, HTTP_FORBIDDEN -> {
                        logger.error("Encountered ${e.statusCode} for chunk $chunkID. Aborting.")
                        break
                    }
                    else -> logger.error("Encountered error downloading chunk $chunkID: ${e.statusCode}")
                }
//            } catch (e: CancellationException) {
//                break
            } catch (e: Exception) {
                cdnPool.returnBrokenConnection(connection)
                logger.error("Encountered unexpected error downloading chunk $chunkID: $e\n${e.stackTraceToString()}")
            }
        } while (writtenBytes <= 0 && isActive)

        if (writtenBytes <= 0) {
            logger.error("Failed to find any server with chunk $chunkID for depot ${depot.depotId}. Aborting.")
            throw CancellationException("Failed to download chunk")
        }

//        logger.debug("Finished downloading, decrypting, and extracting chunk $chunkID")

        try {
            fileStreamData.fileLock.acquire()

            if (fileStreamData.fileStream == null) {
                val fileFinalPath = Paths.get(depot.installDir, file.fileName).toString()
                // Use RandomAccessFile instead of FileOutputStream
                val randomAccessFile = RandomAccessFile(fileFinalPath, "rw")
                fileStreamData.fileStream = randomAccessFile.channel
            }

            fileStreamData.fileStream?.position(chunkInfo.offset)
            // Create a ByteBuffer that only contains the written bytes
            fileStreamData.fileStream?.write(ByteBuffer.wrap(outputChunkData, 0, writtenBytes))
        } finally {
            fileStreamData.fileLock.release()
        }

//        logger.debug("Wrote chunk $chunkID to file ${file.fileName}")

        var remainingChunks: Int
        synchronized(fileStreamData) {
            remainingChunks = fileStreamData.chunksToDownload--
        }
        if (remainingChunks == 0) {
            fileStreamData.fileStream?.close()
            fileStreamData.fileLock.release()
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

        if (remainingChunks == 0) {
            val fileFinalPath = Paths.get(depot.installDir, file.fileName).toString()
            logger.debug("${sizeDownloaded.toFloat() / depotDownloadCounter.completeDownloadSize * 100f}% $fileFinalPath")
        }

        onDownloadProgress?.apply {
            val totalPercent = depotFilesData.depotCounter.sizeDownloaded.toFloat() / depotFilesData.depotCounter.completeDownloadSize
            this(totalPercent)
        }
    }

//    internal suspend fun getStoredManifestOf(depotId: Int, steamClient: SteamClient): DepotManifest? {
//        var oldProtoManifest: DepotManifest? = null
////        val steamData = SteamService.getOrCreateInstance().getSteamData()
//        val lastManifestId = if (steamData.depotManifestIdExists(depotId)) steamData.getLatestDepotManifestId(depotId) else INVALID_MANIFEST_ID
//
//        if (lastManifestId != INVALID_MANIFEST_ID) {
////            val oldManifestFileName = Path.of(Paths.manifestsDirPath(), "${depotId}_${lastManifestId}.bin").toString()
//
//            if (File(oldManifestFileName).exists()) {
//                val expectedChecksum = try {
//                    File("$oldManifestFileName.sha").readBytes()
//                } catch (e: IOException) {
//                    null
//                }
//
//                var currentChecksum: ByteArray?
//                steamClient.configuration.depotManifestProvider.fetchManifest(depotId, lastManifestId)
////                DepotManifest.loadFromFile(oldManifestFileName).apply {
////                    oldProtoManifest = this.first
////                    currentChecksum = this.second
////                }
//
//                if (expectedChecksum == null || !expectedChecksum.contentEquals(currentChecksum)) {
//                    oldProtoManifest = null
//                }
//            }
//        }
//        return oldProtoManifest
//    }

    private fun getFilesManifestOf(
        appId: Int,
        depotId: Int,
        manifestId: Long,
        branch: String,
        depotKey: ByteArray,
        cdnPool: ClientPool,
        parentScope: CoroutineScope
    ): Deferred<Pair<DepotManifest, ByteArray>?> = parentScope.async {
        if (!isActive)
            return@async null

//        logger.debug("Downloading depot manifest...")

        var depotManifest: DepotManifest? = null
        var manifestRequestCode = 0UL
        var manifestRequestCodeExpiration = Instant.MIN

        do {
//            if (!isActive)
//                break

            var connection: Server? = null

            try {
                connection = cdnPool.getConnection().await()

                if (connection == null) continue

                val now = Instant.now()

                // In order to download this manifest, we need the current manifest request code
                // The manifest request code is only valid for a specific period of time
//                if (manifestRequestCode == 0L || now >= manifestRequestCodeExpiration) {
                if (now >= manifestRequestCodeExpiration) {
//                    logger.debug("Manifest request code expired, requesting a new one")
                    val steamContent = steamClient.getHandler(SteamContent::class.java)!!
                    manifestRequestCode = steamContent.getManifestRequestCode(depotId, appId, manifestId, branch, parentScope = parentScope).await()
                    // This code will hopefully be valid for one period following the issuing period
                    manifestRequestCodeExpiration = now.plus(5, ChronoUnit.MINUTES)

                    // If we could not get the manifest code, this is a fatal error
                    if (manifestRequestCode == 0UL) {
                        logger.error("No manifest request code was returned for $depotId $manifestId")
                        cancel("No manifest request code")
//                        throw CancellationException("No manifest request code")
                    }// else
//                        logger.debug("Received manifest request code $manifestRequestCode")
                }

//                logger.debug("Downloading manifest $manifestId from $connection through ${cdnPool.proxyServer?.toString() ?: "no proxy"}")
                depotManifest = cdnPool.cdnClient.downloadManifest(
                    depotId,
                    manifestId,
                    manifestRequestCode,
                    connection,
                    depotKey,
                    cdnPool.proxyServer
                )

                cdnPool.returnConnection(connection)
//            } catch (e: CancellationException) {
//                logger.error("Connection timeout downloading depot manifest $depotId $manifestId")
//                return@async null
            } catch (e: SteamKitWebRequestException) {
                cdnPool.returnBrokenConnection(connection)

                when (e.statusCode) {
                    HTTP_UNAUTHORIZED, HTTP_FORBIDDEN, HTTP_NOT_FOUND ->
                        logger.error("Encountered ${e.statusCode} for depot manifest $depotId $manifestId. Aborting.")
                    else ->
                        logger.error("Encountered error downloading manifest for depot $depotId $manifestId: ${e.statusCode}")
                }
                return@async null
            } catch (e: Exception) {
                cdnPool.returnBrokenConnection(connection)
                logger.error("Encountered error downloading manifest for depot $depotId $manifestId: ${e.message}")
                logger.error(e)
                return@async null
            }
        } while (depotManifest == null && isActive)

        if (depotManifest == null) {
            logger.error("Unable to download manifest $manifestId for depot $depotId")
            return@async null
//            throw CancellationException("Unable to download manifest")
        }

        val newProtoManifest = DepotManifest(depotManifest)
        val checksum = steamClient.configuration.depotManifestProvider.updateManifest(newProtoManifest)
        return@async Pair(newProtoManifest, checksum)
    }
}
