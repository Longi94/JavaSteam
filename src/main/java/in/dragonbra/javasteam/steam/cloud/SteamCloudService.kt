package `in`.dragonbra.javasteam.steam.cloud

import `in`.dragonbra.javasteam.enums.ESteamRealm
import `in`.dragonbra.javasteam.protobufs.steamclient.SteammessagesCloudSteamclient.CCloud_AppLaunchIntent_Request
import `in`.dragonbra.javasteam.protobufs.steamclient.SteammessagesCloudSteamclient.CCloud_AppLaunchIntent_Response
import `in`.dragonbra.javasteam.protobufs.steamclient.SteammessagesCloudSteamclient.CCloud_ClientFileDownload_Request
import `in`.dragonbra.javasteam.protobufs.steamclient.SteammessagesCloudSteamclient.CCloud_ExternalStorageTransferReport_Notification
import `in`.dragonbra.javasteam.protobufs.steamclient.SteammessagesCloudSteamclient.CCloud_GetAppFileChangelist_Request
import `in`.dragonbra.javasteam.rpc.service.Cloud
import `in`.dragonbra.javasteam.steam.handlers.steamunifiedmessages.SteamUnifiedMessages
import `in`.dragonbra.javasteam.steam.steamclient.SteamClient

/**
 * This handler is used for managing user cloud data on Steam.
 *
 * @constructor Initializes a new instance of the [SteamCloudService] class.
 * @param steamClient this instance will be associated with.
 */
class SteamCloudService(private val steamClient: SteamClient) {

    private val cloudService: Cloud

    init {
        val unifiedMessages = steamClient.getHandler(SteamUnifiedMessages::class.java)
            ?: throw NullPointerException("Unable to get SteamUnifiedMessages handler")

        cloudService = unifiedMessages.createService<Cloud>()
    }

    /**
     * Retrieve the file list change for the user files of a certain app since
     * the last sync change.
     *
     * @param appId the ID of the app whose user files to check
     * @param syncedChangeNumber the sync change number
     * @return A [AppFileChangeList] containing the files changed
     */
    @JvmOverloads
    fun getAppFileListChange(appId: Int, syncedChangeNumber: Long = 0): AppFileChangeList {
        val request = CCloud_GetAppFileChangelist_Request.newBuilder().apply {
            this.appid = appId
            this.syncedChangeNumber = syncedChangeNumber
        }

        val response = cloudService.getAppFileChangelist(request.build()).runBlock()

        return AppFileChangeList(response.body)
    }

    /**
     * Request to download a user cloud file of an app
     *
     * @param appId the ID of the app the user file belongs to
     * @param fileName the path to the user file including the prefix
     * @param realm the ESteamRealm value
     * @param forceProxy whether to force proxy
     * @return A [FileDownloadInfo] containing information about how to download the user file
     */
    @JvmOverloads
    fun clientFileDownload(
        appId: Int,
        fileName: String,
        realm: ESteamRealm = ESteamRealm.SteamGlobal,
        forceProxy: Boolean = false
    ): FileDownloadInfo {
        val request = CCloud_ClientFileDownload_Request.newBuilder().apply {
            this.appid = appId
            this.filename = fileName
            this.realm = realm.code()
            this.forceProxy = forceProxy
        }

        val response = cloudService.clientFileDownload(request.build()).runBlock()

        return FileDownloadInfo(response.body)
    }

    fun signalAppLaunchIntent(appId: Int, ignorePendingOperations: Boolean = false): CCloud_AppLaunchIntent_Response.Builder {
        val request = CCloud_AppLaunchIntent_Request.newBuilder().apply {
            this.appid = appId
//            this.clientId =
//            this.machineName =
            this.ignorePendingOperations = ignorePendingOperations
//            this.osType =
//            this.deviceType =
        }

        val response = cloudService.signalAppLaunchIntent(request.build()).runBlock()

        return response.body
    }

    fun externalStorageTransferReport(
        host: String,
        path: String,
        isUpload: Boolean,
        success: Boolean,
        httpStatusCode: Int,
        bytesExpected: Long,
        bytesActual: Long,
        durationMs: Int,
        cellId: Int,
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
}
