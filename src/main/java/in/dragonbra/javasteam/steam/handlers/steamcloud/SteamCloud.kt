package `in`.dragonbra.javasteam.steam.handlers.steamcloud

import com.google.protobuf.ByteString
import `in`.dragonbra.javasteam.base.ClientMsgProtobuf
import `in`.dragonbra.javasteam.base.IPacketMsg
import `in`.dragonbra.javasteam.enums.EMsg
import `in`.dragonbra.javasteam.enums.EOSType
import `in`.dragonbra.javasteam.enums.EResult
import `in`.dragonbra.javasteam.enums.ESteamRealm
import `in`.dragonbra.javasteam.protobufs.steamclient.Enums.EBluetoothDeviceType
import `in`.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserverUfs.CMsgClientUFSGetSingleFileInfo
import `in`.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserverUfs.CMsgClientUFSGetUGCDetails
import `in`.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserverUfs.CMsgClientUFSShareFile
import `in`.dragonbra.javasteam.protobufs.steamclient.SteammessagesCloudSteamclient.CCloud_AppExitSyncDone_Notification
import `in`.dragonbra.javasteam.protobufs.steamclient.SteammessagesCloudSteamclient.CCloud_AppLaunchIntent_Request
import `in`.dragonbra.javasteam.protobufs.steamclient.SteammessagesCloudSteamclient.CCloud_BeginAppUploadBatch_Request
import `in`.dragonbra.javasteam.protobufs.steamclient.SteammessagesCloudSteamclient.CCloud_ClientBeginFileUpload_Request
import `in`.dragonbra.javasteam.protobufs.steamclient.SteammessagesCloudSteamclient.CCloud_ClientCommitFileUpload_Request
import `in`.dragonbra.javasteam.protobufs.steamclient.SteammessagesCloudSteamclient.CCloud_ClientFileDownload_Request
import `in`.dragonbra.javasteam.protobufs.steamclient.SteammessagesCloudSteamclient.CCloud_CompleteAppUploadBatch_Request
import `in`.dragonbra.javasteam.protobufs.steamclient.SteammessagesCloudSteamclient.CCloud_ExternalStorageTransferReport_Notification
import `in`.dragonbra.javasteam.protobufs.steamclient.SteammessagesCloudSteamclient.CCloud_GetAppFileChangelist_Request
import `in`.dragonbra.javasteam.rpc.service.Cloud
import `in`.dragonbra.javasteam.steam.authentication.AuthSession
import `in`.dragonbra.javasteam.steam.handlers.ClientMsgHandler
import `in`.dragonbra.javasteam.steam.handlers.steamcloud.callback.ShareFileCallback
import `in`.dragonbra.javasteam.steam.handlers.steamcloud.callback.SingleFileInfoCallback
import `in`.dragonbra.javasteam.steam.handlers.steamcloud.callback.UGCDetailsCallback
import `in`.dragonbra.javasteam.steam.handlers.steamunifiedmessages.SteamUnifiedMessages
import `in`.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackMsg
import `in`.dragonbra.javasteam.types.AsyncJobSingle
import `in`.dragonbra.javasteam.types.UGCHandle
import `in`.dragonbra.javasteam.util.HardwareUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.future.future
import java.util.Date
import java.util.concurrent.CompletableFuture

/**
 * This handler is used for interacting with remote storage and user generated content.
 */
@Suppress("unused", "DuplicatedCode")
class SteamCloud : ClientMsgHandler() {

    private val cloudService: Cloud by lazy {
        val unifiedMessages = client.getHandler(SteamUnifiedMessages::class.java)
            ?: throw NullPointerException("Unable to get SteamUnifiedMessages handler")
        unifiedMessages.createService<Cloud>()
    }

    /**
     * Requests details for a specific item of user generated content from the Steam servers.
     * Results are returned in a [UGCDetailsCallback].
     *
     * @param ugcId The unique user generated content id.
     * @return The Job ID of the request. This can be used to find the appropriate [UGCDetailsCallback].
     */
    fun requestUGCDetails(ugcId: UGCHandle): AsyncJobSingle<UGCDetailsCallback> {
        val request = ClientMsgProtobuf<CMsgClientUFSGetUGCDetails.Builder>(
            CMsgClientUFSGetUGCDetails::class.java,
            EMsg.ClientUFSGetUGCDetails
        ).apply {
            sourceJobID = client.getNextJobID()

            body.hcontent = ugcId.value
        }

        client.send(request)

        return AsyncJobSingle(client, request.sourceJobID)
    }

    /**
     * Requests details for a specific file in the user's Cloud storage.
     * Results are returned in a [SingleFileInfoCallback].
     *
     * @param appId    The app id of the game.
     * @param filename The path to the file being requested.
     * @return The Job ID of the request. This can be used to find the appropriate [SingleFileInfoCallback].
     */
    fun getSingleFileInfo(appId: Int, filename: String): AsyncJobSingle<SingleFileInfoCallback> {
        val request = ClientMsgProtobuf<CMsgClientUFSGetSingleFileInfo.Builder>(
            CMsgClientUFSGetSingleFileInfo::class.java,
            EMsg.ClientUFSGetSingleFileInfo
        ).apply {
            sourceJobID = client.getNextJobID()

            body.appId = appId
            body.fileName = filename
        }

        client.send(request)

        return AsyncJobSingle(client, request.sourceJobID)
    }

    /**
     * Commit a Cloud file at the given path to make its UGC handle publicly visible.
     * Results are returned in a [ShareFileCallback].
     *
     * @param appId    The app id of the game.
     * @param filename The path to the file being requested.
     * @return The Job ID of the request. This can be used to find the appropriate [ShareFileCallback].
     */
    fun shareFile(appId: Int, filename: String): AsyncJobSingle<ShareFileCallback> {
        val request = ClientMsgProtobuf<CMsgClientUFSShareFile.Builder>(
            CMsgClientUFSShareFile::class.java,
            EMsg.ClientUFSShareFile
        ).apply {
            sourceJobID = client.getNextJobID()

            body.appId = appId
            body.fileName = filename
        }

        client.send(request)

        return AsyncJobSingle(client, request.sourceJobID)
    }

    /**
     * Retrieve the file list change for the user files of a certain app since
     * the last sync change.
     *
     * @param appId              The ID of the app whose user files to check
     * @param syncedChangeNumber The sync change number
     * @return A [AppFileChangeList] containing the files changed
     */
    // JavaSteam Addition
    @JvmOverloads
    fun getAppFileListChange(
        appId: Int,
        syncedChangeNumber: Long = 0,
        parentScope: CoroutineScope = CoroutineScope(Dispatchers.IO),
    ): CompletableFuture<AppFileChangeList> = parentScope.future {
        val request = CCloud_GetAppFileChangelist_Request.newBuilder().apply {
            this.appid = appId
            this.syncedChangeNumber = syncedChangeNumber
        }

        val response = cloudService.getAppFileChangelist(request.build()).runBlock()

        AppFileChangeList(response.body)
    }

    /**
     * Request to download a user cloud file of an app
     *
     * @param appId      The ID of the app the user file belongs to
     * @param fileName   The path to the user file including the prefix
     * @param realm      The ESteamRealm value
     * @param forceProxy Whether to force proxy
     * @return A [FileDownloadInfo] containing information about how to download the user file
     */
    // JavaSteam Addition
    @JvmOverloads
    fun clientFileDownload(
        appId: Int,
        fileName: String,
        realm: ESteamRealm = ESteamRealm.SteamGlobal,
        forceProxy: Boolean = false,
        parentScope: CoroutineScope = CoroutineScope(Dispatchers.IO),
    ): CompletableFuture<FileDownloadInfo> = parentScope.future {
        val request = CCloud_ClientFileDownload_Request.newBuilder().apply {
            this.appid = appId
            this.filename = fileName
            this.realm = realm.code()
            this.forceProxy = forceProxy
        }

        val response = cloudService.clientFileDownload(request.build()).runBlock()

        FileDownloadInfo(response.body)
    }

    /**
     * Begins the user cloud files upload process
     *
     * @param appId         The ID of the app the user files belong to
     * @param machineName   The name of the machine that is uploading the files
     * @param filesToUpload A list of the files to be uploaded including their prefix (ex %GameInstall%)
     * @param filesToDelete A list of the files to be deleted from the cloud including their prefix (ex %GameInstall%)
     * @param clientId      The ID given when authenticating the user [AuthSession.clientID]
     * @param appBuildId    The build ID of the app
     * @return An [AppUploadBatchResponse] containing the batch ID and the app change number
     */
    // JavaSteam Addition
    @JvmOverloads
    fun beginAppUploadBatch(
        appId: Int,
        machineName: String = HardwareUtils.getMachineName(),
        filesToUpload: List<String> = emptyList(),
        filesToDelete: List<String> = emptyList(),
        clientId: Long,
        appBuildId: Long,
        parentScope: CoroutineScope = CoroutineScope(Dispatchers.IO),
    ): CompletableFuture<AppUploadBatchResponse> = parentScope.future {
        val request = CCloud_BeginAppUploadBatch_Request.newBuilder().apply {
            this.appid = appId
            this.machineName = machineName
            this.addAllFilesToUpload(filesToUpload)
            this.addAllFilesToDelete(filesToDelete)
            this.clientId = clientId
            this.appBuildId = appBuildId
        }

        val response = cloudService.beginAppUploadBatch(request.build()).runBlock()

        AppUploadBatchResponse(response.body)
    }

    /**
     * Requests to upload a specific user file within a batch upload request
     *
     * @param appId         The ID of the app whose user file this belongs to
     * @param fileSize      The size of the file in bytes
     * @param rawFileSize   The size of the raw file in bytes
     * @param fileSha       The hash of the file
     * @param timestamp     The timestamp of the file
     * @param uploadBatchId The ID of the upload batch this file upload belongs to (see [beginAppUploadBatch])
     * @return The upload information needed to upload the file in blocks
     */
    // JavaSteam Addition
    fun beginFileUpload(
        appId: Int,
        fileSize: Int,
        rawFileSize: Int,
        fileSha: ByteArray,
        timestamp: Date,
        filename: String,
        platformsToSync: Int = UInt.MAX_VALUE.toInt(),
        cellId: Int = client.cellID,
        canEncrypt: Boolean = true,
        isSharedFile: Boolean = false,
        deprecatedRealm: Int? = null,
        uploadBatchId: Long,
        parentScope: CoroutineScope = CoroutineScope(Dispatchers.IO),
    ): CompletableFuture<FileUploadInfo> = parentScope.future {
        val request = CCloud_ClientBeginFileUpload_Request.newBuilder().apply {
            this.appid = appId
            this.fileSize = fileSize
            this.rawFileSize = rawFileSize
            this.fileSha = ByteString.copyFrom(fileSha)
            this.timeStamp = timestamp.time / 1000L
            this.filename = filename
            this.platformsToSync = platformsToSync
            this.cellId = cellId
            this.canEncrypt = canEncrypt
            this.isSharedFile = isSharedFile
            deprecatedRealm?.let { this.deprecatedRealm = it }
            this.uploadBatchId = uploadBatchId
        }

        val response = cloudService.clientBeginFileUpload(request.build()).runBlock()

        FileUploadInfo(response.body)
    }

    /**
     * Tells Steam that the upload process has completed and if the transfer succeeded
     *
     * @param transferSucceeded Whether the transfer succeeded
     * @param appId             The ID of the app the file upload belonged to
     * @param fileSha           The hash of the file
     * @param filename          The path of the file including the prefix (ex %GameInstall%)
     * @return Whether the file has been committed
     */
    // JavaSteam Addition
    @JvmOverloads
    fun commitFileUpload(
        transferSucceeded: Boolean,
        appId: Int,
        fileSha: ByteArray,
        filename: String,
        parentScope: CoroutineScope = CoroutineScope(Dispatchers.IO),
    ): CompletableFuture<Boolean> = parentScope.future {
        val request = CCloud_ClientCommitFileUpload_Request.newBuilder().apply {
            this.transferSucceeded = transferSucceeded
            this.appid = appId
            this.fileSha = ByteString.copyFrom(fileSha)
            this.filename = filename
        }

        val response = cloudService.clientCommitFileUpload(request.build()).runBlock()
        response.body.fileCommitted
    }

    /**
     * Tells Steam that the app upload batch has completed and waits for an empty response acknowledging
     * the notification
     *
     * @param appId        The ID of the app the upload batch belonged to
     * @param batchId      The ID of the batch
     * @param batchEResult The result of the upload batch
     */
    // JavaSteam Addition
    @JvmOverloads
    fun completeAppUploadBatch(
        appId: Int,
        batchId: Long,
        batchEResult: EResult = EResult.OK,
        parentScope: CoroutineScope = CoroutineScope(Dispatchers.IO),
    ): CompletableFuture<Unit> = parentScope.future {
        val request = CCloud_CompleteAppUploadBatch_Request.newBuilder().apply {
            this.appid = appId
            this.batchId = batchId
            this.batchEresult = batchEResult.code()
        }

        cloudService.completeAppUploadBatchBlocking(request.build()).runBlock()
    }

    /**
     * Lets Steam know we are about to launch an app and Steam responds with any current pending remote operations
     * that we may need to wait for
     *
     * @param appId       The ID of the app about to be launched
     * @param clientId    The ID given when authenticating the user [AuthSession.clientID]
     * @param machineName The name of the machine that is launching the app
     * @param osType      The OS type of the machine launching the app
     * @return A list of the pending remote operations, empty if none
     */
    // JavaSteam Addition
    @JvmOverloads
    fun signalAppLaunchIntent(
        appId: Int,
        clientId: Long,
        machineName: String = HardwareUtils.getMachineName(),
        ignorePendingOperations: Boolean? = null,
        osType: EOSType,
        // I doubt this is EBluetoothDeviceType, but it's the only enum I can find that has to do with device type
        deviceType: EBluetoothDeviceType = EBluetoothDeviceType.k_BluetoothDeviceType_Unknown,
        parentScope: CoroutineScope = CoroutineScope(Dispatchers.IO),
    ): CompletableFuture<List<PendingRemoteOperation>> = parentScope.future {
        val request = CCloud_AppLaunchIntent_Request.newBuilder().apply {
            this.appid = appId
            this.clientId = clientId
            this.machineName = machineName
            ignorePendingOperations?.let { this.ignorePendingOperations = it }
            this.osType = osType.code()
            this.deviceType = deviceType.number
        }

        val response = cloudService.signalAppLaunchIntent(request.build()).runBlock()

        response.body.pendingRemoteOperationsList.map { PendingRemoteOperation(it) }
    }

    /**
     * Notifies Steam that the sync process has finished
     *
     * @param appId    The ID of the app who finished syncing
     * @param clientId The ID given when authenticating the user [AuthSession.clientID]
     */
    // JavaSteam Addition
    fun signalAppExitSyncDone(
        appId: Int,
        clientId: Long,
        uploadsCompleted: Boolean,
        uploadsRequired: Boolean,
    ) {
        val request = CCloud_AppExitSyncDone_Notification.newBuilder().apply {
            this.appid = appId
            this.clientId = clientId
            this.uploadsCompleted = uploadsCompleted
            this.uploadsRequired = uploadsRequired
        }

        cloudService.signalAppExitSyncDone(request.build())
    }

    /**
     * Notifies Steam of the metrics for a past transfer of a specific cloud user file
     *
     * @param host           The URL host of the URL the file was transferred through
     * @param path           The URL path of the URL the file was transferred through
     * @param isUpload       Whether the transfer was an upload or a download
     * @param success        Whether the transfer was successful
     * @param httpStatusCode The HTTP status code of the transfer
     * @param bytesExpected  The expected size in bytes of the file
     * @param bytesActual    The actual size in bytes of the file
     * @param durationMs     The duration of the transfer in milliseconds
     * @param cellId         The cell ID
     * @param proxied        Whether the transfer was through a proxy
     */
    // JavaSteam Addition
    @JvmOverloads
    fun externalStorageTransferReport(
        host: String,
        path: String,
        isUpload: Boolean,
        success: Boolean,
        httpStatusCode: Int,
        bytesExpected: Long,
        bytesActual: Long,
        durationMs: Int,
        cellId: Int = client.cellID,
        proxied: Boolean,
        ipv6Local: Boolean,
        ipv6Remote: Boolean,
        timeToConnectMs: Int,
        timeToSendReqMs: Int,
        timeToFirstByteMs: Int,
        timeToLastByteMs: Int,
    ) {
        val request = CCloud_ExternalStorageTransferReport_Notification.newBuilder().apply {
            this.host = host
            this.path = path
            this.isUpload = isUpload
            this.success = success
            this.httpStatusCode = httpStatusCode
            this.bytesExpected = bytesExpected
            this.bytesActual = bytesActual
            this.durationMs = durationMs
            this.cellid = cellId
            this.proxied = proxied
            this.ipv6Local = ipv6Local
            this.ipv6Remote = ipv6Remote
            this.timeToConnectMs = timeToConnectMs
            this.timeToSendReqMs = timeToSendReqMs
            this.timeToFirstByteMs = timeToFirstByteMs
            this.timeToLastByteMs = timeToLastByteMs
        }

        cloudService.externalStorageTransferReport(request.build())
    }

    /**
     * Handles a client message. This should not be called directly.
     * @param packetMsg The packet message that contains the data.
     */
    override fun handleMsg(packetMsg: IPacketMsg) {
        // ignore messages that we don't have a handler function for
        val callback = getCallback(packetMsg) ?: return

        client.postCallback(callback)
    }

    companion object {
        private fun getCallback(packetMsg: IPacketMsg): CallbackMsg? = when (packetMsg.msgType) {
            EMsg.ClientUFSGetUGCDetailsResponse -> UGCDetailsCallback(packetMsg)
            EMsg.ClientUFSGetSingleFileInfoResponse -> SingleFileInfoCallback(packetMsg)
            EMsg.ClientUFSShareFileResponse -> ShareFileCallback(packetMsg)
            else -> null
        }
    }
}
