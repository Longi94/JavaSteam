package `in`.dragonbra.javasteam.depotdownloader

import `in`.dragonbra.javasteam.depotdownloader.data.AppItem
import `in`.dragonbra.javasteam.depotdownloader.data.ChunkMatch
import `in`.dragonbra.javasteam.depotdownloader.data.DepotDownloadCounter
import `in`.dragonbra.javasteam.depotdownloader.data.DepotDownloadInfo
import `in`.dragonbra.javasteam.depotdownloader.data.DepotFilesData
import `in`.dragonbra.javasteam.depotdownloader.data.DepotProgress
import `in`.dragonbra.javasteam.depotdownloader.data.DownloadItem
import `in`.dragonbra.javasteam.depotdownloader.data.DownloadStatus
import `in`.dragonbra.javasteam.depotdownloader.data.FileProgress
import `in`.dragonbra.javasteam.depotdownloader.data.FileStreamData
import `in`.dragonbra.javasteam.depotdownloader.data.GlobalDownloadCounter
import `in`.dragonbra.javasteam.depotdownloader.data.OverallProgress
import `in`.dragonbra.javasteam.depotdownloader.data.PubFileItem
import `in`.dragonbra.javasteam.depotdownloader.data.UgcItem
import `in`.dragonbra.javasteam.enums.EAccountType
import `in`.dragonbra.javasteam.enums.EAppInfoSection
import `in`.dragonbra.javasteam.enums.EDepotFileFlag
import `in`.dragonbra.javasteam.steam.cdn.ClientLancache
import `in`.dragonbra.javasteam.steam.cdn.Server
import `in`.dragonbra.javasteam.steam.handlers.steamapps.License
import `in`.dragonbra.javasteam.steam.handlers.steamapps.callback.LicenseListCallback
import `in`.dragonbra.javasteam.steam.handlers.steamcloud.callback.UGCDetailsCallback
import `in`.dragonbra.javasteam.steam.steamclient.SteamClient
import `in`.dragonbra.javasteam.types.ChunkData
import `in`.dragonbra.javasteam.types.DepotManifest
import `in`.dragonbra.javasteam.types.FileData
import `in`.dragonbra.javasteam.types.KeyValue
import `in`.dragonbra.javasteam.types.PublishedFileID
import `in`.dragonbra.javasteam.types.UGCHandle
import `in`.dragonbra.javasteam.util.Adler32
import `in`.dragonbra.javasteam.util.SteamKitWebRequestException
import `in`.dragonbra.javasteam.util.Strings
import `in`.dragonbra.javasteam.util.log.LogManager
import `in`.dragonbra.javasteam.util.log.Logger
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsChannel
import io.ktor.http.HttpHeaders
import io.ktor.utils.io.core.readAvailable
import io.ktor.utils.io.core.remaining
import io.ktor.utils.io.readRemaining
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.future.future
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.withContext
import kotlinx.coroutines.yield
import okio.Buffer
import okio.FileSystem
import okio.Path
import okio.Path.Companion.toPath
import okio.buffer
import org.apache.commons.lang3.SystemUtils
import java.io.Closeable
import java.io.IOException
import java.lang.IllegalStateException
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.concurrent.*
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import kotlin.collections.mutableListOf
import kotlin.collections.set
import kotlin.text.toLongOrNull

/**
 * [ContentDownloader] is a JavaSteam module that is able to download Games, Workshop Items, and other content from Steam.
 * @param steamClient an instance of [SteamClient]
 * @param licenses a list of licenses the logged-in user has. This is provided by [LicenseListCallback]
 * @param debug enable or disable logging through [LogManager]
 * @param useLanCache try and detect a local Steam Cache server.
 * @param maxDownloads the number of simultaneous downloads.
 *
 * @author Oxters
 * @author Lossy
 * @since Oct 29, 2024
 */
@Suppress("unused")
class ContentDownloader @JvmOverloads constructor(
    private val steamClient: SteamClient,
    private val licenses: List<License>, // To be provided from [LicenseListCallback]
    private val debug: Boolean = false, // Enable debugging features, such as logging
    private val useLanCache: Boolean = false, // Try and detect a lan cache server.
    private var maxDownloads: Int = 8, // Max concurrent downloads
) : Closeable {

    companion object {
        const val INVALID_APP_ID: Int = Int.MAX_VALUE
        const val INVALID_DEPOT_ID: Int = Int.MAX_VALUE
        const val INVALID_MANIFEST_ID: Long = Long.MAX_VALUE

        const val CONFIG_DIR: String = ".DepotDownloader"
        const val DEFAULT_BRANCH: String = "public"
        const val DEFAULT_DOWNLOAD_DIR: String = "depots"

        val STAGING_DIR: Path = CONFIG_DIR.toPath() / "staging"
    }

    // What is a PriorityQueue?

    private val filesystem: FileSystem by lazy { FileSystem.SYSTEM }
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val items = CopyOnWriteArrayList(ArrayList<DownloadItem>())
    private val listeners = CopyOnWriteArrayList<IDownloadListener>()
    private var logger: Logger? = null
    private val isStarted: AtomicBoolean = AtomicBoolean(false)
    private val processingChannel = Channel<DownloadItem>(Channel.UNLIMITED)
    private val remainingItems = AtomicInteger(0)
    private val lastFileProgressUpdate = ConcurrentHashMap<String, Long>()
    private val progressUpdateInterval = 500L // ms

    private var steam3: Steam3Session? = null

    private var cdnClientPool: CDNClientPool? = null

    private var config: Config = Config()

    // region [REGION] Private data classes.

    private data class NetworkChunkItem(
        val fileStreamData: FileStreamData,
        val fileData: FileData,
        val chunk: ChunkData,
        val totalChunksForFile: Int,
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
        if (debug) {
            logger = LogManager.getLogger(ContentDownloader::class.java)
        }

        logger?.debug("DepotDownloader launched with ${licenses.size} for account")

        steam3 = Steam3Session(steamClient, debug)
    }

    // region [REGION] Downloading Operations
    @Throws(IllegalStateException::class)
    suspend fun downloadPubFile(appId: Int, publishedFileId: Long) {
        val details = requireNotNull(
            steam3!!.getPublishedFileDetails(appId, PublishedFileID(publishedFileId))
        ) { "Pub File Null" }

        if (!details.fileUrl.isNullOrBlank()) {
            downloadWebFile(appId, details.filename, details.fileUrl)
        } else if (details.hcontentFile > 0) {
            downloadApp(
                appId = appId,
                depotManifestIds = listOf(appId to details.hcontentFile),
                branch = DEFAULT_BRANCH,
                os = null,
                arch = null,
                language = null,
                lv = false,
                isUgc = true,
            )
        } else {
            logger?.error("Unable to locate manifest ID for published file $publishedFileId")
        }
    }

    suspend fun downloadUGC(
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
            logger?.error("Unable to query UGC details for $ugcId from an anonymous account")
        }

        if (!details?.url.isNullOrBlank()) {
            downloadWebFile(appId = appId, fileName = details.fileName, url = details.url)
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
    suspend fun downloadWebFile(appId: Int, fileName: String, url: String) {
        val (success, installDir) = createDirectories(appId, 0, appId)

        if (!success) {
            logger?.debug("Error: Unable to create install directories!")
            return
        }

        val stagingDir = installDir!! / "staging"
        val fileStagingPath = stagingDir / fileName
        val fileFinalPath = installDir / fileName

        filesystem.createDirectories(fileFinalPath.parent!!)
        filesystem.createDirectories(fileStagingPath.parent!!)

        HttpClient.getClient(maxDownloads).use { client ->
            logger?.debug("Starting download of $fileName...")

            val response = client.get(url)
            val channel = response.bodyAsChannel()

            val totalBytes = response.headers[HttpHeaders.ContentLength]?.toLongOrNull()

            logger?.debug("File size: ${totalBytes?.let { Util.formatBytes(it) } ?: "Unknown"}")

            filesystem.sink(fileStagingPath).buffer().use { sink ->
                val buffer = Buffer()
                while (!channel.isClosedForRead) {
                    val packet = channel.readRemaining(DEFAULT_BUFFER_SIZE.toLong())
                    if (!packet.exhausted()) {
                        // Read from Ktor packet into Okio buffer
                        val bytesRead = packet.remaining.toInt()
                        val tempArray = ByteArray(bytesRead)
                        packet.readAvailable(tempArray)
                        buffer.write(tempArray)

                        // Write from buffer to sink
                        sink.writeAll(buffer)
                    }
                }
            }

            logger?.debug("Download completed.")
        }

        if (filesystem.exists(fileFinalPath)) {
            logger?.debug("Deleting $fileFinalPath")
            filesystem.delete(fileFinalPath)
        }

        try {
            filesystem.atomicMove(fileStagingPath, fileFinalPath)
            logger?.debug("File '$fileStagingPath' moved to final location: $fileFinalPath")
        } catch (e: IOException) {
            throw e
        }
    }

    // L4D2 (app) supports LV
    @Throws(IllegalStateException::class)
    suspend fun downloadApp(
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
        cdnClientPool = CDNClientPool(steam3!!, appId, scope, debug)

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
                throw ContentDownloaderException("App $appId ($contentName) is not available from this account.")
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
                @Suppress("VariableInitializerIsRedundant")
                var id = INVALID_DEPOT_ID

                if (depotSection.children.isEmpty()) {
                    return@forEach
                }

                id = depotSection.name?.toIntOrNull() ?: return@forEach

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
                throw ContentDownloaderException("Couldn't find any depots to download for app $appId")
            }

            if (depotIdsFound.size < depotIdsExpected.size) {
                val remainingDepotIds = depotIdsExpected.subtract(depotIdsFound.toSet())
                throw ContentDownloaderException("Depot ${remainingDepotIds.joinToString(", ")} not listed for app $appId")
            }
        }

        val infos = mutableListOf<DepotDownloadInfo>()

        depotManifestIds.forEach { (depotId, manifestId) ->
            val info = getDepotInfo(depotId, appId, manifestId, branch)
            if (info != null) {
                infos.add(info)
            }
        }

        downloadSteam3(infos)
    }

    @Throws(IllegalStateException::class)
    private suspend fun getDepotInfo(
        depotId: Int,
        appId: Int,
        manifestId: Long,
        branch: String,
    ): DepotDownloadInfo? {
        var manifestId = manifestId
        var branch = branch

        if (appId != INVALID_APP_ID) {
            steam3!!.requestAppInfo(appId)
        }

        if (!accountHasAccess(appId, depotId)) {
            logger?.error("Depot $depotId is not available from this account.")
            return null
        }

        if (manifestId == INVALID_MANIFEST_ID) {
            manifestId = getSteam3DepotManifest(depotId, appId, branch)

            if (manifestId == INVALID_MANIFEST_ID && !branch.equals(DEFAULT_BRANCH, true)) {
                logger?.error("Warning: Depot $depotId does not have branch named \"$branch\". Trying $DEFAULT_BRANCH branch.")
                branch = DEFAULT_BRANCH
                manifestId = getSteam3DepotManifest(depotId, appId, branch)
            }

            if (manifestId == INVALID_MANIFEST_ID) {
                logger?.error("Depot $depotId missing public subsection or manifest section.")
                return null
            }
        }

        steam3!!.requestDepotKey(depotId, appId)

        val depotKey = steam3!!.depotKeys[depotId]
        if (depotKey == null) {
            logger?.error("No valid depot key for $depotId, unable to download.")
            return null
        }

        val uVersion = getSteam3AppBuildNumber(appId, branch)

        val (success, installDir) = createDirectories(depotId, uVersion, appId)
        if (!success) {
            logger?.error("Error: Unable to create install directories!")
            return null
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
                logger?.error("App $appId, Depot $depotId has depotfromapp of $otherAppId!")
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
            logger?.error("Branch $branch for depot $depotId was not found, either it does not exist or it has a password.")
            return INVALID_MANIFEST_ID
        }

        if (!steam3!!.appBetaPasswords.containsKey(branch)) {
            // Submit the password to Steam now to get encryption keys
            steam3!!.checkAppBetaPassword(appId, config.betaPassword!!)

            if (!steam3!!.appBetaPasswords.containsKey(branch)) {
                logger?.error("Error: Password was invalid for branch $branch (or the branch does not exist)")
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

    @Throws(IllegalStateException::class)
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
                    val gameName = getAppName(appId)

                    if (gameName.isBlank()) {
                        throw IOException("Game name is blank, cannot create directory")
                    }

                    installDir = DEFAULT_DOWNLOAD_DIR.toPath() / gameName

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
                    val gameName = getAppName(appId)

                    if (gameName.isBlank()) {
                        throw IOException("Game name is blank, cannot create directory")
                    }

                    installDir = config.installPath!! / gameName

                    filesystem.createDirectories(installDir)
                } else {
                    installDir = config.installPath!!
                }

                filesystem.createDirectories(installDir / CONFIG_DIR)
                filesystem.createDirectories(installDir / STAGING_DIR)
            }
        } catch (e: IOException) {
            logger?.error(e)
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
            else -> throw ContentDownloaderException("${section.name} not implemented")
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

        val licenseQuery = arrayListOf<Int>()
        if (steamID.accountType == EAccountType.AnonUser) {
            licenseQuery.add(17906)
        } else {
            licenseQuery.addAll(licenses.map { it.packageID }.distinct())
        }

        steam3!!.requestPackageInfo(licenseQuery)

        licenseQuery.forEach { license ->
            steam3!!.packageInfo[license]?.value?.let { pkg ->
                val appIds = pkg.keyValues["appids"].children.map { it.asInteger() }
                val depotIds = pkg.keyValues["depotids"].children.map { it.asInteger() }
                if (depotId in appIds) {
                    return true
                }
                if (depotId in depotIds) {
                    return true
                }
            }
        }

        // Check if this app is free to download without a license
        val info = getSteam3AppSection(appId, EAppInfoSection.Common)

        return info != null && info["FreeToDownload"].asBoolean()
    }

    private suspend fun downloadSteam3(depots: List<DepotDownloadInfo>): Unit = coroutineScope {
        cdnClientPool?.updateServerList()

        val downloadCounter = GlobalDownloadCounter()
        val depotsToDownload = ArrayList<DepotFilesData>(depots.size)
        val allFileNamesAllDepots = hashSetOf<String>()

        var completedDepots = 0

        // First, fetch all the manifests for each depot (including previous manifests) and perform the initial setup
        depots.forEach { depot ->
            val depotFileData = processDepotManifestAndFiles(depot, downloadCounter)

            if (depotFileData != null) {
                depotsToDownload.add(depotFileData)
                allFileNamesAllDepots.union(depotFileData.allFileNames)
            }

            ensureActive()
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

        depotsToDownload.forEach { depotFileData ->
            downloadSteam3DepotFiles(downloadCounter, depotFileData, allFileNamesAllDepots)

            completedDepots++

            val snapshot = synchronized(downloadCounter) {
                OverallProgress(
                    currentItem = completedDepots,
                    totalItems = depotsToDownload.size,
                    totalBytesDownloaded = downloadCounter.totalBytesUncompressed,
                    totalBytesExpected = downloadCounter.completeDownloadSize,
                    status = DownloadStatus.DOWNLOADING
                )
            }

            notifyListeners { listener ->
                listener.onOverallProgress(progress = snapshot)
            }
        }

        logger?.debug(
            "Total downloaded: ${downloadCounter.totalBytesCompressed} bytes " +
                "(${downloadCounter.totalBytesUncompressed} bytes uncompressed) from ${depots.size} depots"
        )
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

                do {
                    ensureActive()

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
                                logger?.error("Failed to get CDN auth token: ${e.message}")
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
                                cancel("manifestRequestCode is 0UL")
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
                    } catch (e: CancellationException) {
                        // logger?.error("Connection timeout downloading depot manifest ${depot.depotId} ${depot.manifestId}. Retrying.")
                        logger?.error(e)
                        break
                    } catch (e: SteamKitWebRequestException) {
                        // If the CDN returned 403, attempt to get a cdn auth if we didn't yet
                        if (e.statusCode == 403 && !steam3!!.cdnAuthTokens.containsKey(depot.depotId to connection!!.host)) {
                            steam3!!.requestCDNAuthToken(depot.appId, depot.depotId, connection)

                            cdnClientPool!!.returnConnection(connection)

                            continue
                        }

                        cdnClientPool!!.returnBrokenConnection(connection)

                        // Unauthorized || Forbidden
                        if (e.statusCode == 401 || e.statusCode == 403) {
                            logger?.error("Encountered ${depot.depotId} for depot manifest ${depot.manifestId} ${e.statusCode}. Aborting.")
                            break
                        }

                        // NotFound
                        if (e.statusCode == 404) {
                            logger?.error("Encountered 404 for depot manifest ${depot.depotId} ${depot.manifestId}. Aborting.")
                            break
                        }

                        logger?.error("Encountered error downloading depot manifest ${depot.depotId} ${depot.manifestId}: ${e.statusCode}")
                    } catch (e: Exception) {
                        cdnClientPool!!.returnBrokenConnection(connection)
                        logger?.error("Encountered error downloading manifest for depot ${depot.depotId} ${depot.manifestId}: ${e.message}")
                    }
                } while (newManifest == null)

                if (newManifest == null) {
                    logger?.error("\nUnable to download manifest ${depot.manifestId} for depot ${depot.depotId}")
                    cancel()
                }

                // Throw the cancellation exception if requested so that this task is marked failed
                ensureActive()

                Util.saveManifestToFile(configDir, newManifest!!)
            }
        }

        logger?.debug("Manifest ${depot.manifestId} (${newManifest.creationTime})")

        if (config.downloadManifestOnly) {
            Util.dumpManifestToTextFile(depot, newManifest)
            return@withContext null
        }

        val stagingDir = depot.installDir / STAGING_DIR

        val filesAfterExclusions = coroutineScope {
            newManifest.files.filter { file ->
                async { testIsFileIncluded(file.fileName) }.await()
            }
        }
        val allFileNames = HashSet<String>(filesAfterExclusions.size)

        // Pre-process
        filesAfterExclusions.forEach { file ->
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

                downloadCounter.completeDownloadSize += file.totalSize
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
        )
    }

    @OptIn(DelicateCoroutinesApi::class)
    private suspend fun downloadSteam3DepotFiles(
        downloadCounter: GlobalDownloadCounter,
        depotFilesData: DepotFilesData,
        allFileNamesAllDepots: HashSet<String>,
    ) = withContext(Dispatchers.IO) {
        val depot = depotFilesData.depotDownloadInfo
        val depotCounter = depotFilesData.depotCounter

        logger?.debug("Downloading depot ${depot.depotId}")

        val files = depotFilesData.filteredFiles.filter { !it.flags.contains(EDepotFileFlag.Directory) }
        val networkChunkQueue = Channel<NetworkChunkItem>(Channel.UNLIMITED)

        try {
            val filesCompleted = AtomicInteger(0)
            val lastReportedProgress = AtomicInteger(0)
            coroutineScope {
                // First parallel loop - process files and enqueue chunks
                files.map { file ->
                    async {
                        yield() // Does this matter if its before?
                        downloadSteam3DepotFile(
                            downloadCounter = downloadCounter,
                            depotFilesData = depotFilesData,
                            file = file,
                            networkChunkQueue = networkChunkQueue
                        )

                        val completed = filesCompleted.incrementAndGet()
                        if (completed % 10 == 0 || completed == files.size) {
                            val snapshot = synchronized(depotCounter) {
                                DepotProgress(
                                    depotId = depot.depotId,
                                    filesCompleted = completed,
                                    totalFiles = files.size,
                                    bytesDownloaded = depotCounter.sizeDownloaded,
                                    totalBytes = depotCounter.completeDownloadSize,
                                    status = DownloadStatus.PREPARING // Changed from DOWNLOADING
                                )
                            }

                            val lastReported = lastReportedProgress.get()
                            if (completed > lastReported &&
                                lastReportedProgress.compareAndSet(
                                    lastReported,
                                    completed
                                )
                            ) {
                                notifyListeners { listener ->
                                    listener.onDepotProgress(depot.depotId, snapshot)
                                }
                            }
                        }
                    }
                }.awaitAll()

                // Close the channel to signal no more items will be added
                networkChunkQueue.close()

                // After all files allocated, send one update showing preparation complete
                val progressReporter = launch {
                    while (true) {
                        delay(1000)
                        val snapshot = synchronized(depotCounter) {
                            DepotProgress(
                                depotId = depot.depotId,
                                filesCompleted = files.size,
                                totalFiles = files.size,
                                bytesDownloaded = depotCounter.sizeDownloaded,
                                totalBytes = depotCounter.completeDownloadSize,
                                status = DownloadStatus.DOWNLOADING
                            )
                        }
                        notifyListeners { listener ->
                            listener.onDepotProgress(depot.depotId, snapshot)
                        }
                    }
                }

                // Second parallel loop - process chunks from queue
                try {
                    List(maxDownloads) {
                        async {
                            for (item in networkChunkQueue) {
                                downloadSteam3DepotFileChunk(
                                    downloadCounter = downloadCounter,
                                    depotFilesData = depotFilesData,
                                    file = item.fileData,
                                    fileStreamData = item.fileStreamData,
                                    chunk = item.chunk
                                )
                            }
                        }
                    }.awaitAll()
                } finally {
                    progressReporter.cancel()
                }
            }
        } finally {
            if (!networkChunkQueue.isClosedForSend) {
                networkChunkQueue.close()
            }
        }

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
        logger?.debug("Depot ${depot.depotId} - Downloaded ${depotCounter.depotBytesCompressed} bytes (${depotCounter.depotBytesUncompressed} bytes uncompressed)")
    }

    private suspend fun downloadSteam3DepotFile(
        downloadCounter: GlobalDownloadCounter,
        depotFilesData: DepotFilesData,
        file: FileData,
        networkChunkQueue: Channel<NetworkChunkItem>,
    ) = withContext(Dispatchers.IO) {
        ensureActive()

        val depot = depotFilesData.depotDownloadInfo
        val stagingDir = depotFilesData.stagingDir
        val depotDownloadCounter = depotFilesData.depotCounter
        val oldProtoManifest = depotFilesData.previousManifest

        var oldManifestFile: FileData? = null
        if (oldProtoManifest != null) {
            oldManifestFile = oldProtoManifest.files.singleOrNull { it.fileName == file.fileName }
        }

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
                filesystem.openReadWrite(fileFinalPath).use { handle ->
                    handle.resize(file.totalSize)
                }
            } catch (e: IOException) {
                throw ContentDownloaderException("Failed to allocate file $fileFinalPath: ${e.message}")
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
                            // Read the chunk data into a byte array
                            val length = match.oldChunk.uncompressedLength
                            val buffer = ByteArray(length)
                            handle.read(match.oldChunk.offset, buffer, 0, length)

                            // Calculate Adler32 checksum
                            val adler = Adler32.calculate(buffer)

                            // Convert checksum to byte array for comparison
                            val checksumBytes = Buffer().apply {
                                writeIntLe(match.oldChunk.checksum)
                            }.readByteArray()
                            val calculatedChecksumBytes = Buffer().apply {
                                writeIntLe(adler)
                            }.readByteArray()

                            if (!calculatedChecksumBytes.contentEquals(checksumBytes)) {
                                neededChunks.add(match.newChunk)
                            } else {
                                copyChunks.add(match)
                            }
                        }
                    }

                    if (!hashMatches || neededChunks.isNotEmpty()) {
                        filesystem.atomicMove(fileFinalPath, fileStagingPath)

                        try {
                            filesystem.openReadOnly(fileStagingPath).use { oldHandle ->
                                filesystem.openReadWrite(fileFinalPath).use { newHandle ->
                                    try {
                                        newHandle.resize(file.totalSize)
                                    } catch (ex: IOException) {
                                        throw ContentDownloaderException(
                                            "Failed to resize file to expected size $fileFinalPath: ${ex.message}"
                                        )
                                    }

                                    for (match in copyChunks) {
                                        val tmp = ByteArray(match.oldChunk.uncompressedLength)
                                        oldHandle.read(match.oldChunk.offset, tmp, 0, tmp.size)
                                        newHandle.write(match.newChunk.offset, tmp, 0, tmp.size)
                                    }
                                }
                            }
                        } catch (e: Exception) {
                            logger?.error(e)
                        }

                        filesystem.delete(fileStagingPath)
                    }
                }
            } else {
                // No old manifest or file not in old manifest. We must validate.
                filesystem.openReadWrite(fileFinalPath).use { handle ->
                    val fileSize = filesystem.metadata(fileFinalPath).size ?: 0L
                    if (fileSize.toULong() != file.totalSize.toULong()) {
                        try {
                            handle.resize(file.totalSize)
                        } catch (ex: IOException) {
                            throw ContentDownloaderException(
                                "Failed to allocate file $fileFinalPath: ${ex.message}"
                            )
                        }
                    }

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

                synchronized(downloadCounter) {
                    downloadCounter.completeDownloadSize -= file.totalSize
                }

                return@withContext
            }

            val sizeOnDisk = file.totalSize - neededChunks!!.sumOf { it.uncompressedLength }
            synchronized(depotDownloadCounter) {
                depotDownloadCounter.sizeDownloaded += sizeOnDisk
            }

            synchronized(downloadCounter) {
                downloadCounter.completeDownloadSize -= sizeOnDisk
            }
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
            networkChunkQueue.send(
                NetworkChunkItem(
                    fileStreamData = fileStreamData,
                    fileData = file,
                    chunk = chunk,
                    totalChunksForFile = neededChunks!!.size
                )
            )
        }
    }

    private suspend fun downloadSteam3DepotFileChunk(
        downloadCounter: GlobalDownloadCounter,
        depotFilesData: DepotFilesData,
        file: FileData,
        fileStreamData: FileStreamData,
        chunk: ChunkData,
    ): Unit = withContext(Dispatchers.IO) {
        ensureActive()

        val depot = depotFilesData.depotDownloadInfo
        val depotDownloadCounter = depotFilesData.depotCounter

        val chunkID = Strings.toHex(chunk.chunkID)

        var written = 0
        val chunkBuffer = ByteArray(chunk.uncompressedLength)

        try {
            do {
                ensureActive()

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
                            logger?.error("Failed to get CDN auth token: ${e.message}")
                        }
                    }

                    logger?.debug("Downloading chunk $chunkID from $connection with ${cdnClientPool!!.proxyServer ?: "no proxy"}")

                    written = cdnClientPool!!.cdnClient!!.downloadDepotChunk(
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
                } catch (e: CancellationException) {
                    logger?.error(e)
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

                    cdnClientPool!!.returnBrokenConnection(connection)

                    // Unauthorized || Forbidden
                    if (e.statusCode == 401 || e.statusCode == 403) {
                        logger?.error("Encountered ${e.statusCode} for chunk $chunkID. Aborting.")
                        break
                    }

                    logger?.error("Encountered error downloading chunk $chunkID: ${e.statusCode}")
                } catch (e: Exception) {
                    cdnClientPool!!.returnBrokenConnection(connection)
                    logger?.error("Encountered unexpected error downloading chunk $chunkID", e)
                }
            } while (written == 0)

            if (written == 0) {
                logger?.error("Failed to find any server with chunk ${chunk.chunkID} for depot ${depot.depotId}. Aborting.")
                cancel()
            }

            // Throw the cancellation exception if requested so that this task is marked failed
            ensureActive()

            try {
                fileStreamData.fileLock.lock()

                if (fileStreamData.fileHandle == null) {
                    val fileFinalPath = depot.installDir / file.fileName
                    fileStreamData.fileHandle = filesystem.openReadWrite(fileFinalPath)
                }

                fileStreamData.fileHandle!!.write(chunk.offset, chunkBuffer, 0, written)
            } finally {
                fileStreamData.fileLock.unlock()
            }
        } finally {
        }

        val remainingChunks = fileStreamData.chunksToDownload.decrementAndGet()
        if (remainingChunks == 0) {
            fileStreamData.fileHandle?.close()
        }

        var sizeDownloaded = 0L
        synchronized(depotDownloadCounter) {
            sizeDownloaded = depotDownloadCounter.sizeDownloaded + written.toLong()
            depotDownloadCounter.sizeDownloaded = sizeDownloaded
            depotDownloadCounter.depotBytesCompressed += chunk.compressedLength
            depotDownloadCounter.depotBytesUncompressed += chunk.uncompressedLength
        }

        synchronized(downloadCounter) {
            downloadCounter.totalBytesCompressed += chunk.compressedLength
            downloadCounter.totalBytesUncompressed += chunk.uncompressedLength
        }

        val now = System.currentTimeMillis()
        val fileKey = "${depot.depotId}:${file.fileName}"
        val lastUpdate = lastFileProgressUpdate[fileKey] ?: 0L

        if (now - lastUpdate >= progressUpdateInterval || remainingChunks == 0) {
            lastFileProgressUpdate[fileKey] = now

            val totalChunks = file.chunks.size
            val completedChunks = totalChunks - remainingChunks

            // Approximate bytes based on completion ratio
            val approximateBytesDownloaded = (file.totalSize * completedChunks) / totalChunks

            notifyListeners { listener ->
                listener.onFileProgress(
                    depotId = depot.depotId,
                    fileName = file.fileName,
                    progress = FileProgress(
                        fileName = file.fileName,
                        bytesDownloaded = approximateBytesDownloaded,
                        totalBytes = file.totalSize,
                        chunksCompleted = completedChunks,
                        totalChunks = totalChunks,
                        status = if (remainingChunks == 0) DownloadStatus.COMPLETED else DownloadStatus.DOWNLOADING
                    )
                )
            }
        }

        if (remainingChunks == 0) {
            val fileFinalPath = depot.installDir / file.fileName
            val percentage = (sizeDownloaded / depotDownloadCounter.completeDownloadSize.toFloat()) * 100.0f
            logger?.debug("%.2f%% %s".format(percentage, fileFinalPath))
        }
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

    // endregion

    // region [REGION] Listener Operations

    fun addListener(listener: IDownloadListener) {
        listeners.add(listener)
    }

    fun removeListener(listener: IDownloadListener) {
        listeners.remove(listener)
    }

    private fun notifyListeners(action: (IDownloadListener) -> Unit) {
        listeners.forEach { listener -> action(listener) }
    }

    // endregion

    // region [REGION] Array Operations

    fun getItems(): List<DownloadItem> = items.toList()

    fun size(): Int = items.size

    fun isEmpty(): Boolean = items.isEmpty()

    fun get(index: Int): DownloadItem? = items.getOrNull(index)

    fun contains(item: DownloadItem): Boolean = items.contains(item)

    fun indexOf(item: DownloadItem): Int = items.indexOf(item)

    fun addAll(items: List<DownloadItem>) {
        items.forEach(::add)
    }

    fun add(item: DownloadItem) {
        val index = items.size
        items.add(item)

        if (isStarted.get()) {
            remainingItems.incrementAndGet()
            scope.launch { processingChannel.send(item) }
        }

        notifyListeners { it.onItemAdded(item, index) }
    }

    fun addFirst(item: DownloadItem) {
        if (isStarted.get()) {
            logger?.debug("Cannot add item when started.")
            return
        }

        try {
            items.add(0, item)
            notifyListeners { it.onItemAdded(item, 0) }
        } catch (e: Exception) {
            logger?.error(e)
        }
    }

    fun addAt(index: Int, item: DownloadItem): Boolean {
        if (isStarted.get()) {
            logger?.debug("Cannot addAt item when started.")
            return false
        }

        return try {
            items.add(index, item)
            notifyListeners { it.onItemAdded(item, index) }
            true
        } catch (e: IndexOutOfBoundsException) {
            false
        }
    }

    fun removeFirst(): DownloadItem? {
        if (isStarted.get()) {
            logger?.debug("Cannot removeFirst item when started.")
            return null
        }

        return try {
            if (items.isNotEmpty()) {
                val item = items.removeAt(0)
                notifyListeners { it.onItemRemoved(item, 0) }
                item
            } else {
                null
            }
        } catch (e: IndexOutOfBoundsException) {
            logger?.error(e)
            null
        }
    }

    fun removeLast(): DownloadItem? {
        if (isStarted.get()) {
            logger?.debug("Cannot removeLast item when started.")
            return null
        }

        return try {
            if (items.isNotEmpty()) {
                val lastIndex = items.size - 1
                val item = items.removeAt(lastIndex)
                notifyListeners { it.onItemRemoved(item, lastIndex) }
                item
            } else {
                null
            }
        } catch (e: IndexOutOfBoundsException) {
            logger?.error(e)
            null
        }
    }

    fun remove(item: DownloadItem): Boolean {
        if (isStarted.get()) {
            logger?.debug("Cannot remove item when started.")
            return false
        }

        val index = items.indexOf(item)
        return try {
            if (index >= 0) {
                items.removeAt(index)
                notifyListeners { it.onItemRemoved(item, index) }
                true
            } else {
                false
            }
        } catch (e: IndexOutOfBoundsException) {
            logger?.error(e)
            false
        }
    }

    fun removeAt(index: Int): DownloadItem? {
        if (isStarted.get()) {
            logger?.debug("Cannot removeAt item when started.")
            return null
        }

        return try {
            val item = items.removeAt(index)
            notifyListeners { it.onItemRemoved(item, index) }
            item
        } catch (e: IndexOutOfBoundsException) {
            null
        }
    }

    fun moveItem(fromIndex: Int, toIndex: Int): Boolean {
        if (isStarted.get()) {
            logger?.debug("Cannot moveItem item when started.")
            return false
        }

        return try {
            val item = items.removeAt(fromIndex)
            items.add(toIndex, item)
            true
        } catch (e: IndexOutOfBoundsException) {
            false
        }
    }

    fun clear() {
        if (isStarted.get()) {
            logger?.debug("Cannot clear item when started.")
            return
        }

        val oldItems = items.toList()
        items.clear()

        notifyListeners { it.onQueueCleared(oldItems) }
    }

    // endregion

    /**
     * Some android emulators prefer using "Windows", so this will set downloading to prefer the Windows version.
     */
    fun setAndroidEmulation(value: Boolean) {
        if (isStarted.get()) {
            logger?.error("Can't set android emulation value once started.")
            return
        }

        config = config.copy(androidEmulation = value)

        notifyListeners { it.onAndroidEmulation(config.androidEmulation) }
    }

    @Throws(IllegalStateException::class)
    fun start(): CompletableFuture<Boolean> = scope.future {
        if (isStarted.getAndSet(true)) {
            logger?.debug("Downloading already started.")
            return@future false
        }

        val initialItems = items.toList()
        if (initialItems.isEmpty()) {
            logger?.debug("No items to download")
            return@future false
        }

        // Send initial items
        remainingItems.set(initialItems.size)
        initialItems.forEach { processingChannel.send(it) }

        if (ClientLancache.useLanCacheServer) {
            logger?.debug("Detected Lan-Cache server! Downloads will be directed through the Lancache.")

            // Increasing the number of concurrent downloads when the cache is detected since the downloads will likely
            // be served much faster than over the internet.  Steam internally has this behavior as well.
            if (maxDownloads == 8) {
                maxDownloads = 25
            }
        }

        repeat(remainingItems.get()) {
            // Process exactly this many
            ensureActive()

            // Obtain the next item in queue.
            val item = processingChannel.receive()

            try {
                runBlocking {
                    if (useLanCache) {
                        ClientLancache.detectLancacheServer()
                    }

                    // Set some configuration values, first.
                    config = config.copy(
                        downloadManifestOnly = item.downloadManifestOnly,
                        installPath = item.installDirectory?.toPath(),
                        installToGameNameDirectory = item.installToGameNameDirectory,
                    )

                    // Sequential looping.
                    when (item) {
                        is PubFileItem -> {
                            if (item.pubfile == INVALID_MANIFEST_ID) {
                                logger?.debug("Invalid Pub File ID for ${item.appId}")
                                return@runBlocking
                            }

                            logger?.debug("Downloading PUB File for ${item.appId}")

                            notifyListeners { it.onDownloadStarted(item) }
                            downloadPubFile(item.appId, item.pubfile)
                        }

                        is UgcItem -> {
                            if (item.ugcId == INVALID_MANIFEST_ID) {
                                logger?.debug("Invalid UGC ID for ${item.appId}")
                                return@runBlocking
                            }

                            logger?.debug("Downloading UGC File for ${item.appId}")

                            notifyListeners { it.onDownloadStarted(item) }
                            downloadUGC(item.appId, item.ugcId)
                        }

                        is AppItem -> {
                            val branch = item.branch ?: DEFAULT_BRANCH
                            config = config.copy(betaPassword = item.branchPassword)

                            if (!config.betaPassword.isNullOrBlank() && branch.isBlank()) {
                                logger?.error("Error: Cannot specify 'branchpassword' when 'branch' is not specified.")
                                return@runBlocking
                            }

                            config = config.copy(downloadAllPlatforms = item.downloadAllPlatforms)

                            val os = item.os

                            if (config.downloadAllPlatforms && !os.isNullOrBlank()) {
                                logger?.error("Error: Cannot specify `os` when `all-platforms` is specified.")
                                return@runBlocking
                            }

                            config = config.copy(downloadAllArchs = item.downloadAllArchs)

                            val arch = item.osArch

                            if (config.downloadAllArchs && !arch.isNullOrBlank()) {
                                logger?.error("Error: Cannot specify `osarch` when `all-archs` is specified.")
                                return@runBlocking
                            }

                            config = config.copy(downloadAllLanguages = item.downloadAllLanguages)

                            val language = item.language

                            if (config.downloadAllLanguages && !language.isNullOrBlank()) {
                                logger?.error("Error: Cannot specify `language` when `all-languages` is specified.")
                                return@runBlocking
                            }

                            val lv = item.lowViolence

                            val depotManifestIds = mutableListOf<Pair<Int, Long>>()
                            val isUGC = false

                            val depotIdList = item.depot
                            val manifestIdList = item.manifest

                            if (manifestIdList.isNotEmpty()) {
                                if (depotIdList.size != manifestIdList.size) {
                                    logger?.error("Error: `manifest` requires one id for every `depot` specified")
                                    return@runBlocking
                                }
                                val zippedDepotManifest = depotIdList.zip(manifestIdList) { depotId, manifestId ->
                                    Pair(depotId, manifestId)
                                }
                                depotManifestIds.addAll(zippedDepotManifest)
                            } else {
                                depotManifestIds.addAll(
                                    depotIdList.map { depotId ->
                                        Pair(depotId, INVALID_MANIFEST_ID)
                                    }
                                )
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
                                lv = lv,
                                isUgc = isUGC,
                            )
                        }
                    }

                    notifyListeners { it.onDownloadCompleted(item) }
                }
            } catch (e: IOException) {
                logger?.error("Error downloading item ${item.appId}: ${e.message}", e)

                notifyListeners { it.onDownloadFailed(item, e) }

                throw e
            }
        }

        return@future true
    }

    override fun close() {
        isStarted.set(false)

        HttpClient.close()

        items.clear()
        processingChannel.close()

        lastFileProgressUpdate.clear()
        listeners.clear()

        steam3?.close()
        steam3 = null

        cdnClientPool?.close()
        cdnClientPool = null

        logger = null
    }
}
