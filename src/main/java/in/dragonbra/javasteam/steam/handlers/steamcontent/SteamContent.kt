package `in`.dragonbra.javasteam.steam.handlers.steamcontent

import `in`.dragonbra.javasteam.base.IPacketMsg
import `in`.dragonbra.javasteam.protobufs.steamclient.SteammessagesContentsystemSteamclient.CContentServerDirectory_GetCDNAuthToken_Request
import `in`.dragonbra.javasteam.protobufs.steamclient.SteammessagesContentsystemSteamclient.CContentServerDirectory_GetClientUpdateHosts_Request
import `in`.dragonbra.javasteam.protobufs.steamclient.SteammessagesContentsystemSteamclient.CContentServerDirectory_GetDepotPatchInfo_Request
import `in`.dragonbra.javasteam.protobufs.steamclient.SteammessagesContentsystemSteamclient.CContentServerDirectory_GetManifestRequestCode_Request
import `in`.dragonbra.javasteam.protobufs.steamclient.SteammessagesContentsystemSteamclient.CContentServerDirectory_GetPeerContentInfo_Request
import `in`.dragonbra.javasteam.protobufs.steamclient.SteammessagesContentsystemSteamclient.CContentServerDirectory_GetServersForSteamPipe_Request
import `in`.dragonbra.javasteam.protobufs.steamclient.SteammessagesContentsystemSteamclient.CContentServerDirectory_RequestPeerContentServer_Request
import `in`.dragonbra.javasteam.rpc.service.ContentServerDirectory
import `in`.dragonbra.javasteam.steam.cdn.Server
import `in`.dragonbra.javasteam.steam.handlers.ClientMsgHandler
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
     * TODO kdoc
     * @param appId
     * @param depotId
     * @param sourceManifestId
     * @param targetManifestId
     * @return A [DepotPatchInfo]
     */
    fun getDepotPatchInfo(
        appId: Int,
        depotId: Int,
        sourceManifestId: Long,
        targetManifestId: Long,
        parentScope: CoroutineScope,
    ): Deferred<DepotPatchInfo> = parentScope.async {
        val request = CContentServerDirectory_GetDepotPatchInfo_Request.newBuilder().apply {
            this.appid = appId
            this.depotid = depotId
            this.sourceManifestid = sourceManifestId
            this.targetManifestid = targetManifestId
        }.build()

        val message = contentService.getDepotPatchInfo(request).await()
        val response = message.body.build()

        return@async DepotPatchInfo(
            isAvailable = response.isAvailable,
            patchSize = response.patchSize,
            patchedChunksSize = response.patchedChunksSize
        )
    }

    /**
     * TODO kdoc
     * @param cachedSignature
     * @return A [ClientUpdateHosts]
     */
    fun getClientUpdateHosts(
        cachedSignature: String,
        parentScope: CoroutineScope,
    ): Deferred<ClientUpdateHosts> = parentScope.async {
        val request = CContentServerDirectory_GetClientUpdateHosts_Request.newBuilder().apply {
            this.cachedSignature = cachedSignature
        }.build()

        val message = contentService.getClientUpdateHosts(request).await()
        val response = message.body.build()

        return@async ClientUpdateHosts(response.hostsKv, response.validUntilTime, response.ipCountry)
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
     * TODO kdoc
     * @param remoteClientId
     * @param steamId
     * @param serverRemoteClientId
     * @param appId
     * @param currentBuildId
     * @return A [RequestPeerContentServer]
     */
    fun requestPeerContentServer(
        remoteClientId: Long,
        steamId: Long,
        serverRemoteClientId: Long,
        appId: Int,
        currentBuildId: Int = 0,
        parentScope: CoroutineScope,
    ): Deferred<RequestPeerContentServer> = parentScope.async {
        val request = CContentServerDirectory_RequestPeerContentServer_Request.newBuilder().apply {
            this.remoteClientId = remoteClientId
            this.steamid = steamId
            this.serverRemoteClientId = serverRemoteClientId
            this.appId = appId
            this.currentBuildId = currentBuildId
        }.build()

        val message = contentService.requestPeerContentServer(request).await()
        val response = message.body.build()

        return@async RequestPeerContentServer(
            serverPort = response.serverPort,
            installedDepots = response.installedDepotsList,
            accessToken = response.accessToken,
        )
    }

    /**
     * TODO kdoc
     * @param remoteClientId
     * @param steamId
     * @param serverRemoteClientId
     * @return A [GetPeerContentInfo]
     */
    fun getPeerContentInfo(
        remoteClientId: Long,
        steamId: Long,
        serverRemoteClientId: Long,
        parentScope: CoroutineScope,
    ): Deferred<GetPeerContentInfo> = parentScope.async {
        val request = CContentServerDirectory_GetPeerContentInfo_Request.newBuilder().apply {
            this.remoteClientId = remoteClientId
            this.steamid = steamId
            this.serverRemoteClientId = serverRemoteClientId
        }.build()

        val message = contentService.getPeerContentInfo(request).await()
        val response = message.body.build()

        return@async GetPeerContentInfo(
            appIds = response.appidsList,
            ipPublic = response.ipPublic,
        )
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
