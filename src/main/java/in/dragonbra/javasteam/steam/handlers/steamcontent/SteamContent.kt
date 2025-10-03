package `in`.dragonbra.javasteam.steam.handlers.steamcontent

import `in`.dragonbra.javasteam.base.IPacketMsg
import `in`.dragonbra.javasteam.protobufs.steamclient.SteammessagesContentsystemSteamclient.CContentServerDirectory_GetCDNAuthToken_Request
import `in`.dragonbra.javasteam.protobufs.steamclient.SteammessagesContentsystemSteamclient.CContentServerDirectory_GetManifestRequestCode_Request
import `in`.dragonbra.javasteam.protobufs.steamclient.SteammessagesContentsystemSteamclient.CContentServerDirectory_GetServersForSteamPipe_Request
import `in`.dragonbra.javasteam.rpc.service.ContentServerDirectory
import `in`.dragonbra.javasteam.steam.cdn.Server
import `in`.dragonbra.javasteam.steam.handlers.ClientMsgHandler
import `in`.dragonbra.javasteam.steam.handlers.steamcontent.CDNAuthToken
import `in`.dragonbra.javasteam.steam.handlers.steamunifiedmessages.SteamUnifiedMessages
import `in`.dragonbra.javasteam.steam.webapi.ContentServerDirectoryService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async

/**
 * This handler is used for interacting with content server directory on the Steam network.
 */
class SteamContent : ClientMsgHandler() {
    private val contentService: ContentServerDirectory by lazy {
        val unifiedMessages = client.getHandler(SteamUnifiedMessages::class.java)
            ?: throw NullPointerException("Unable to get SteamUnifiedMessages handler")
        unifiedMessages.createService<ContentServerDirectory>()
    }

    /**
     * Load a list of servers from the Content Server Directory Service.
     * This is an alternative to `ContentServerDirectoryService.loadAsync` (does not exist in JS atm).
     *
     * @param cellId Preferred steam cell id
     * @param maxNumServers Max number of servers to return.
     * @return A [List] of [Server]s.
     */
    fun getServersForSteamPipe(
        cellId: Int? = null,
        maxNumServers: Int? = null,
        parentScope: CoroutineScope,
    ): Deferred<List<Server>> = parentScope.async {
        val request = CContentServerDirectory_GetServersForSteamPipe_Request.newBuilder().apply {
            this.cellId = cellId ?: client.cellID ?: 0
            maxNumServers?.let { this.maxServers = it }
        }.build()

        val message = contentService.getServersForSteamPipe(request).await()
        val response = message.body.build()

        return@async ContentServerDirectoryService.convertServerList(response)
    }

    /**
     * Request the manifest request code for the specified arguments.
     *
     * @param depotId The DepotID to request a manifest request code for.
     * @param appId The AppID parent of the DepotID.
     * @param manifestId The ManifestID that will be downloaded.
     * @param branch The branch name this manifest belongs to.
     * @param branchPasswordHash The branch password. TODO: (SK) how is it hashed?
     * @return Returns the manifest request code, it may be zero if it was not granted.
     */
    fun getManifestRequestCode(
        depotId: Int,
        appId: Int,
        manifestId: Long,
        branch: String? = null,
        branchPasswordHash: String? = null,
        parentScope: CoroutineScope,
    ): Deferred<Long> = parentScope.async {
        var localBranch = branch
        var localBranchPasswordHash = branchPasswordHash

        if (localBranch.equals("public", ignoreCase = true)) {
            localBranch = null
            localBranchPasswordHash = null
        }

        if (localBranchPasswordHash != null && localBranch == null) {
            throw IllegalArgumentException("Branch name may not be null if password is provided.")
        }

        val request = CContentServerDirectory_GetManifestRequestCode_Request.newBuilder().apply {
            this.appId = appId
            this.depotId = depotId
            this.manifestId = manifestId
            localBranch?.let { this.appBranch = it }
            localBranchPasswordHash?.let { this.branchPasswordHash = it }
        }.build()

        val message = contentService.getManifestRequestCode(request).await()
        val response = message.body.build()

        return@async response.manifestRequestCode
    }

    /**
     * Request product information for an app or package
     * Results are returned in a [CDNAuthToken].
     *
     * @param app App id requested.
     * @param depot Depot id requested.
     * @param hostName CDN host name being requested.
     * @return The [CDNAuthToken] containing the result.
     */
    fun getCDNAuthToken(
        app: Int,
        depot: Int,
        hostName: String,
        parentScope: CoroutineScope,
    ): Deferred<CDNAuthToken> = parentScope.async {
        val request = CContentServerDirectory_GetCDNAuthToken_Request.newBuilder().apply {
            this.appId = app
            this.depotId = depot
            this.hostName = hostName
        }.build()

        val message = contentService.getCDNAuthToken(request).await()

        return@async CDNAuthToken(message)
    }

    /**
     * Handles a client message. This should not be called directly.
     *
     * @param packetMsg The packet message that contains the data.
     */
    override fun handleMsg(packetMsg: IPacketMsg) {
        // not used
    }
}
