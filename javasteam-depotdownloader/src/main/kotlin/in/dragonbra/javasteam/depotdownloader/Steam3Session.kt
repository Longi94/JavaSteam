package `in`.dragonbra.javasteam.depotdownloader

import `in`.dragonbra.javasteam.enums.EResult
import `in`.dragonbra.javasteam.protobufs.steamclient.SteammessagesPublishedfileSteamclient
import `in`.dragonbra.javasteam.rpc.service.PublishedFile
import `in`.dragonbra.javasteam.steam.cdn.Server
import `in`.dragonbra.javasteam.steam.handlers.steamapps.PICSProductInfo
import `in`.dragonbra.javasteam.steam.handlers.steamapps.PICSRequest
import `in`.dragonbra.javasteam.steam.handlers.steamapps.SteamApps
import `in`.dragonbra.javasteam.steam.handlers.steamcloud.SteamCloud
import `in`.dragonbra.javasteam.steam.handlers.steamcloud.callback.UGCDetailsCallback
import `in`.dragonbra.javasteam.steam.handlers.steamcontent.CDNAuthToken
import `in`.dragonbra.javasteam.steam.handlers.steamcontent.SteamContent
import `in`.dragonbra.javasteam.steam.handlers.steamunifiedmessages.SteamUnifiedMessages
import `in`.dragonbra.javasteam.steam.handlers.steamuser.SteamUser
import `in`.dragonbra.javasteam.steam.steamclient.SteamClient
import `in`.dragonbra.javasteam.types.KeyValue
import `in`.dragonbra.javasteam.types.PublishedFileID
import `in`.dragonbra.javasteam.types.UGCHandle
import `in`.dragonbra.javasteam.util.log.LogManager
import `in`.dragonbra.javasteam.util.log.Logger
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.Closeable
import java.util.concurrent.ConcurrentHashMap

/**
 * @author Lossy
 * @since Oct 1, 2025
 */
class Steam3Session(
    private val steamClient: SteamClient,
    debug: Boolean,
) : Closeable {

    private var logger: Logger? = null

    internal val appTokens = ConcurrentHashMap<Int, Long>()
    internal val packageTokens = ConcurrentHashMap<Int, Long>()
    internal val depotKeys = ConcurrentHashMap<Int, ByteArray>()
    internal val cdnAuthTokens = ConcurrentHashMap<Pair<Int, String>, CompletableDeferred<CDNAuthToken>>()
    internal val appInfo = ConcurrentHashMap<Int, Optional<PICSProductInfo>>()
    internal val packageInfo = ConcurrentHashMap<Int, Optional<PICSProductInfo>>()
    internal val appBetaPasswords = ConcurrentHashMap<String, ByteArray>()

    private var unifiedMessages: SteamUnifiedMessages? = null
    internal var steamUser: SteamUser? = null
    internal var steamContent: SteamContent? = null
    internal var steamApps: SteamApps? = null
    internal var steamCloud: SteamCloud? = null
    internal var steamPublishedFile: PublishedFile? = null

    // ConcurrentHashMap can't have nullable Keys or Values
    internal data class Optional<T>(val value: T?)

    init {
        if (debug) {
            logger = LogManager.getLogger(Steam3Session::class.java)
        }

        unifiedMessages = requireNotNull(steamClient.getHandler<SteamUnifiedMessages>())
        steamApps = requireNotNull(steamClient.getHandler<SteamApps>())
        steamCloud = requireNotNull(steamClient.getHandler<SteamCloud>())
        steamContent = requireNotNull(steamClient.getHandler<SteamContent>())
        steamPublishedFile = requireNotNull(unifiedMessages?.createService<PublishedFile>())
        steamUser = requireNotNull(steamClient.getHandler<SteamUser>())
    }

    override fun close() {
        logger?.debug("Closing...")

        unifiedMessages = null
        steamUser = null
        steamContent = null
        steamApps = null
        steamCloud = null
        steamPublishedFile = null

        cdnAuthTokens.values.forEach { it.cancel() }
        cdnAuthTokens.clear()

        depotKeys.values.forEach { it.fill(0) }
        depotKeys.clear()
        appBetaPasswords.values.forEach { it.fill(0) }
        appBetaPasswords.clear()

        appTokens.clear()
        packageTokens.clear()
        appInfo.clear()
        packageInfo.clear()

        logger = null
    }

    suspend fun requestAppInfo(appId: Int, bForce: Boolean = false) {
        if (appInfo.containsKey(appId) && !bForce) {
            return
        }

        val appTokens = steamApps!!.picsGetAccessTokens(appId).await()

        if (appTokens.appTokensDenied.contains(appId)) {
            logger?.error("Insufficient privileges to get access token for app $appId")
        }

        appTokens.appTokens.forEach { tokenDict ->
            this.appTokens[tokenDict.key] = tokenDict.value
        }

        val request = PICSRequest(appId)

        this.appTokens[appId]?.let { token ->
            request.accessToken = token
        }

        val appInfoMultiple = steamApps!!.picsGetProductInfo(request).await()

        logger?.debug(
            "requestAppInfo($appId, $bForce) with \n" +
                "${appTokens.appTokens.size} appTokens, \n" +
                "${appTokens.appTokensDenied.size} appTokensDenied, \n" +
                "${appTokens.packageTokens.size} packageTokens, and \n" +
                "${appTokens.packageTokensDenied} packageTokensDenied. \n" +
                "picsGetProductInfo result size: ${appInfoMultiple.results.size}"
        )

        appInfoMultiple.results.forEach { appInfo ->
            appInfo.apps.forEach { appValue ->
                val app = appValue.value
                this.appInfo[app.id] = Optional(app)
            }
            appInfo.unknownApps.forEach { app ->
                this.appInfo[app] = Optional(null)
            }
        }
    }

    // TODO race condition (??)
    private val packageInfoMutex = Mutex()
    suspend fun requestPackageInfo(packageIds: List<Int>) {
        packageInfoMutex.withLock {
            // I have a silly race condition???
            val packages = packageIds.filter { !packageInfo.containsKey(it) }

            if (packages.isEmpty()) return

            val packageRequests = arrayListOf<PICSRequest>()

            packages.forEach { pkg ->
                val request = PICSRequest(id = pkg)

                packageTokens[pkg]?.let { token ->
                    request.accessToken = token
                }

                packageRequests.add(request)
            }

            val packageInfoMultiple = steamApps!!.picsGetProductInfo(emptyList(), packageRequests).await()

            logger?.debug(
                "requestPackageInfo(packageIds =${packageIds.size}) \n" +
                    "picsGetProductInfo result size: ${packageInfoMultiple.results.size} "
            )

            packageInfoMultiple.results.forEach { pkgInfo ->
                pkgInfo.packages.forEach { pkgValue ->
                    val pkg = pkgValue.value
                    packageInfo[pkg.id] = Optional(pkg)
                }
                pkgInfo.unknownPackages.forEach { pkgValue ->
                    packageInfo[pkgValue] = Optional(null)
                }
            }
        }
    }

    suspend fun requestFreeAppLicense(appId: Int): Boolean {
        try {
            val resultInfo = steamApps!!.requestFreeLicense(appId).await()

            logger?.debug("requestFreeAppLicense($appId) has result ${resultInfo.result}")

            return resultInfo.grantedApps.contains(appId)
        } catch (e: Exception) {
            logger?.error("Failed to request FreeOnDemand license for app $appId: ${e.message}")
            return false
        }
    }

    suspend fun requestDepotKey(depotId: Int, appId: Int = 0) {
        if (depotKeys.containsKey(depotId)) {
            return
        }

        val depotKey = steamApps!!.getDepotDecryptionKey(depotId, appId).await()

        logger?.debug(
            "requestDepotKey($depotId, $appId) " +
                "Got depot key for ${depotKey.depotID} result: ${depotKey.result}"
        )

        if (depotKey.result != EResult.OK) {
            return
        }

        depotKeys[depotKey.depotID] = depotKey.depotKey
    }

    suspend fun getDepotManifestRequestCode(
        depotId: Int,
        appId: Int,
        manifestId: Long,
        branch: String,
    ): ULong = withContext(Dispatchers.IO) {
        val requestCode = steamContent!!.getManifestRequestCode(
            depotId = depotId,
            appId = appId,
            manifestId = manifestId,
            branch = branch,
            branchPasswordHash = null,
            parentScope = this // TODO am I passing this right?
        ).await().toULong()

        if (requestCode == 0UL) {
            logger?.error("No manifest request code was returned for depot $depotId from app $appId, manifest $manifestId")

            if (steamClient.isDisconnected) {
                logger?.debug("Suggestion: Try logging in with -username as old manifests may not be available for anonymous accounts.")
            }
        } else {
            logger?.debug("Got manifest request code for depot $depotId from app $appId, manifest $manifestId, result: $requestCode")
        }

        logger?.debug(
            "getDepotManifestRequestCode($depotId, $appId, $manifestId, $branch) " +
                "got request code $requestCode"
        )

        return@withContext requestCode
    }

    suspend fun requestCDNAuthToken(appId: Int, depotId: Int, server: Server) = withContext(Dispatchers.IO) {
        val cdnKey = depotId to server.host!!

        if (cdnAuthTokens.containsKey(cdnKey)) {
            return@withContext
        }

        val completion = CompletableDeferred<CDNAuthToken>()

        val existing = cdnAuthTokens.putIfAbsent(cdnKey, completion)
        if (existing != null) {
            return@withContext
        }

        logger?.debug("Requesting CDN auth token for ${server.host}")

        try {
            val cdnAuth = steamContent!!.getCDNAuthToken(appId, depotId, server.host!!, this).await()

            logger?.debug("Got CDN auth token for ${server.host} result: ${cdnAuth.result} (expires ${cdnAuth.expiration})")

            if (cdnAuth.result != EResult.OK) {
                cdnAuthTokens.remove(cdnKey) // Remove failed promise
                completion.completeExceptionally(Exception("Failed to get CDN auth token: ${cdnAuth.result}"))
                return@withContext
            }

            completion.complete(cdnAuth)
        } catch (e: Exception) {
            logger?.error(e)
            cdnAuthTokens.remove(cdnKey) // Remove failed promise
            completion.completeExceptionally(e)
        }
    }

    suspend fun checkAppBetaPassword(appId: Int, password: String) {
        val appPassword = steamApps!!.checkAppBetaPassword(appId, password).await()

        logger?.debug(
            "checkAppBetaPassword($appId, <password>)," +
                "retrieved ${appPassword.betaPasswords.size} beta keys with result: ${appPassword.result}"
        )

        appPassword.betaPasswords.forEach { entry ->
            this.appBetaPasswords[entry.key] = entry.value
        }
    }

    suspend fun getPrivateBetaDepotSection(appId: Int, branch: String): KeyValue {
        // Should be filled by CheckAppBetaPassword
        val branchPassword = appBetaPasswords[branch] ?: return KeyValue()

        // Should be filled by RequestAppInfo
        val accessToken = appTokens[appId] ?: 0L

        val privateBeta = steamApps!!.picsGetPrivateBeta(appId, accessToken, branch, branchPassword).await()

        logger?.debug("getPrivateBetaDepotSection($appId, $branch) result: ${privateBeta.result}")

        return privateBeta.depotSection
    }

    @Throws(ContentDownloaderException::class)
    suspend fun getPublishedFileDetails(
        appId: Int,
        pubFile: PublishedFileID,
    ): SteammessagesPublishedfileSteamclient.PublishedFileDetails? {
        val pubFileRequest =
            SteammessagesPublishedfileSteamclient.CPublishedFile_GetDetails_Request.newBuilder().apply {
                this.appid = appId
                this.addPublishedfileids(pubFile.toLong())
            }.build()

        val details = steamPublishedFile!!.getDetails(pubFileRequest).await()

        logger?.debug("requestUGCDetails($appId, $pubFile) result: ${details.result}")

        if (details.result == EResult.OK) {
            return details.body.publishedfiledetailsBuilderList.firstOrNull()?.build()
        }

        throw ContentDownloaderException("EResult ${details.result.code()} (${details.result}) while retrieving file details for pubfile $pubFile.")
    }

    suspend fun getUGCDetails(ugcHandle: UGCHandle): UGCDetailsCallback? {
        val callback = steamCloud!!.requestUGCDetails(ugcHandle).await()

        logger?.debug("requestUGCDetails($ugcHandle) result: ${callback.result}")

        if (callback.result == EResult.OK) {
            return callback
        } else if (callback.result == EResult.FileNotFound) {
            return null
        }

        throw ContentDownloaderException("EResult ${callback.result.code()} (${callback.result}) while retrieving UGC details for ${ugcHandle.value}.")
    }
}
