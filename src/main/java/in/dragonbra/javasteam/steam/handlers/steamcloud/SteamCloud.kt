package `in`.dragonbra.javasteam.steam.handlers.steamcloud

import com.google.protobuf.ByteString
import `in`.dragonbra.javasteam.base.ClientMsgProtobuf
import `in`.dragonbra.javasteam.base.IPacketMsg
import `in`.dragonbra.javasteam.enums.EMsg
import `in`.dragonbra.javasteam.enums.EOSType
import `in`.dragonbra.javasteam.enums.EPlatformType
import `in`.dragonbra.javasteam.enums.EResult
import `in`.dragonbra.javasteam.enums.ESteamRealm
import `in`.dragonbra.javasteam.protobufs.steamclient.Enums.EBluetoothDeviceType
import `in`.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientmetricsSteamclient.CClientMetrics_CloudAppSyncStats_Notification
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
import `in`.dragonbra.javasteam.rpc.service.ClientMetrics
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
    private val clientMetrics: ClientMetrics by lazy {
        val unifiedMessages = client.getHandler(SteamUnifiedMessages::class.java)
            ?: throw NullPointerException("Unable to get SteamUnifiedMessages handler")
        unifiedMessages.createService<ClientMetrics>()
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

        val response = cloudService.getAppFileChangelist(request.build()).await()

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

        val response = cloudService.clientFileDownload(request.build()).await()

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

        val response = cloudService.beginAppUploadBatch(request.build()).await()

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
        cellId: Int = client.cellID!!,
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

        val response = cloudService.clientBeginFileUpload(request.build()).await()

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

        val response = cloudService.clientCommitFileUpload(request.build()).await()
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

        cloudService.completeAppUploadBatchBlocking(request.build()).await()
    }

    /**
     * Notify Steam of the stats of the sync that just occurred. The values with Ac in them,
     * I believe, are for [Steam Auto Cloud](https://partner.steamgames.com/doc/features/cloud#steam_auto-cloud).
     *
     * @param appId The ID of the app whose user files were synced
     * @param filesUploaded The number of files that were uploaded in the sync
     * @param filesDownloaded The number of files that were downloaded in the sync
     * @param filesDeleted The number of files that were deleted in the sync
     * @param bytesUploaded The total number of bytes that were uploaded in the sync
     * @param bytesDownloaded The total number of bytes that were downloaded in the sync
     * @param microsecTotal The total time the sync took in micro-seconds
     * @param microsecDeleteFiles The time the sync took to delete all the required files in micro-seconds
     * @param microsecDownloadFiles The time the sync took to download all the required files in micro-seconds
     * @param microsecUploadFiles The time the sync took to upload all the required files in micro-seconds
     */
    // JavaSteam Addition
    @JvmOverloads
    fun appCloudSyncStats(
        appId: Int,
        platformType: EPlatformType,
        preload: Boolean = false,
        blockingAppLaunch: Boolean,
        filesUploaded: Int = 0,
        filesDownloaded: Int = 0,
        filesDeleted: Int = 0,
        bytesUploaded: Long = 0,
        bytesDownloaded: Long = 0,
        microsecTotal: Long,
        microsecInitCaches: Long,
        microsecValidateState: Long,
        microsecAcLaunch: Long = 0,
        microsecAcPrepUserFiles: Long = 0,
        microsecAcExit: Long = 0,
        microsecBuildSyncList: Long,
        microsecDeleteFiles: Long = 0,
        microsecDownloadFiles: Long = 0,
        microsecUploadFiles: Long = 0,
        hardwareType: Int = 1,
        filesManaged: Int,
    ) {
        val request = CClientMetrics_CloudAppSyncStats_Notification.newBuilder().apply {
            this.appId = appId
            this.platformType = platformType.code()
            this.preload = preload
            this.blockingAppLaunch = blockingAppLaunch
            if (filesUploaded > 0) {
                this.filesUploaded = filesUploaded
            }
            if (filesDownloaded > 0) {
                this.filesDownloaded = filesDownloaded
            }
            if (filesDeleted > 0) {
                this.filesDeleted = filesDeleted
            }
            if (bytesUploaded > 0) {
                this.bytesUploaded = bytesUploaded
            }
            if (bytesDownloaded > 0) {
                this.bytesDownloaded = bytesDownloaded
            }
            this.microsecTotal = microsecTotal
            this.microsecInitCaches = microsecInitCaches
            this.microsecValidateState = microsecValidateState
            if (microsecAcLaunch > 0) {
                this.microsecAcLaunch = microsecAcLaunch
            }
            if (microsecAcPrepUserFiles > 0) {
                this.microsecAcPrepUserFiles = microsecAcPrepUserFiles
            }
            if (microsecAcExit > 0) {
                this.microsecAcExit = microsecAcExit
            }
            this.microsecBuildSyncList = microsecBuildSyncList
            if (microsecDeleteFiles > 0) {
                this.microsecDeleteFiles = microsecDeleteFiles
            }
            if (microsecDownloadFiles > 0) {
                this.microsecDownloadFiles = microsecDownloadFiles
            }
            if (microsecUploadFiles > 0) {
                this.microsecUploadFiles = microsecUploadFiles
            }
            this.hardwareType = hardwareType
            this.filesManaged = filesManaged
        }
        clientMetrics.clientCloudAppSyncStats(request.build())
    }

    /**
     * Lets Steam know we are about to launch an app and Steam responds with any current pending remote operations
     * that we may need to wait for
     *
     * @param appId                   The ID of the app about to be launched
     * @param clientId                The ID given when authenticating the user [AuthSession.clientID]
     * @param machineName             The name of the machine that is launching the app
     * @param ignorePendingOperations Should current remote operations be ignored and app be launched anyway
     * @param osType                  The OS type of the machine launching the app
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

        val response = cloudService.signalAppLaunchIntent(request.build()).await()

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
        cellId: Int = client.cellID!!,
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
