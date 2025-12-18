package `in`.dragonbra.javasteam.steam.handlers.steamclientcommunication

import `in`.dragonbra.javasteam.base.IPacketMsg
import `in`.dragonbra.javasteam.enums.EGamingDeviceType
import `in`.dragonbra.javasteam.enums.EOSType
import `in`.dragonbra.javasteam.enums.EResult
import `in`.dragonbra.javasteam.enums.ESteamRealm
import `in`.dragonbra.javasteam.protobufs.webui.ServiceClientcomm.CClientComm_EnableOrDisableDownloads_Request
import `in`.dragonbra.javasteam.protobufs.webui.ServiceClientcomm.CClientComm_GetAllClientLogonInfo_Request
import `in`.dragonbra.javasteam.protobufs.webui.ServiceClientcomm.CClientComm_GetClientAppList_Request
import `in`.dragonbra.javasteam.protobufs.webui.ServiceClientcomm.CClientComm_GetClientInfo_Request
import `in`.dragonbra.javasteam.protobufs.webui.ServiceClientcomm.CClientComm_GetClientLogonInfo_Request
import `in`.dragonbra.javasteam.protobufs.webui.ServiceClientcomm.CClientComm_InstallClientApp_Request
import `in`.dragonbra.javasteam.protobufs.webui.ServiceClientcomm.CClientComm_LaunchClientApp_Request
import `in`.dragonbra.javasteam.protobufs.webui.ServiceClientcomm.CClientComm_SetClientAppUpdateState_Request
import `in`.dragonbra.javasteam.protobufs.webui.ServiceClientcomm.CClientComm_UninstallClientApp_Request
import `in`.dragonbra.javasteam.rpc.service.ClientComm
import `in`.dragonbra.javasteam.steam.handlers.ClientMsgHandler
import `in`.dragonbra.javasteam.steam.handlers.steamunifiedmessages.SteamUnifiedMessages
import `in`.dragonbra.javasteam.util.JavaSteamAddition
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async

/**
 * Allows controlling of other running Steam clients.
 */
@Suppress("unused")
@JavaSteamAddition
class SteamClientCommunication : ClientMsgHandler() {

    private val clientComm: ClientComm by lazy {
        val unifiedMessages = client.getHandler<SteamUnifiedMessages>()
            ?: throw NullPointerException("Unable to get SteamUnifiedMessages handler")
        unifiedMessages.createService<ClientComm>()
    }

    /**
     * Retrieves information about all active Steam clients connected to the network.
     * Note: This returns all connected clients. Filter the results based on OS or device type as needed.
     * @return Information about all active client sessions with recommended refetch interval.
     */
    fun getAllClientLogonInfo(): Deferred<AllClientLogonInfo> = client.defaultScope.async {
        val request = CClientComm_GetAllClientLogonInfo_Request.newBuilder().build()

        val message = clientComm.getAllClientLogonInfo(request).await()
        val response = message.body.build()

        return@async AllClientLogonInfo(
            sessions = response.sessionsList.map {
                AllClientLogonInfoSession(
                    clientInstanceId = it.clientInstanceid,
                    protocolVersion = it.protocolVersion,
                    osName = it.osName,
                    machineName = it.machineName,
                    osType = EOSType.from(it.osType),
                    deviceType = EGamingDeviceType.from(it.deviceType),
                    realm = ESteamRealm.from(it.realm),
                )
            },
            refetchIntervalSec = response.refetchIntervalSec,
        )
    }

    /**
     * Return the list of applications of the remote device.
     * This is not the list of downloaded apps, but the whole "available" list with a flag indicating about it being downloaded.
     * @param remoteId The remote session ID of the client to query.
     * @param filters filters to choose, see [InstalledAppsFilter].
     * @param language language for localized app names (e.g., "english", "french").
     */
    @JvmOverloads
    fun getClientAppList(
        remoteId: Long,
        filters: InstalledAppsFilter = InstalledAppsFilter.None,
        language: String = "english",
    ): Deferred<ClientAppList> = client.defaultScope.async {
        val request = CClientComm_GetClientAppList_Request.newBuilder().apply {
            this.clientInstanceid = remoteId
            this.language = language
            this.includeClientInfo = true
            this.fields = "games"
            this.filters = when (filters) {
                InstalledAppsFilter.None -> "none"
                InstalledAppsFilter.Changing -> "changing"
            }
        }.build()

        val message = clientComm.getClientAppList(request).await()
        val response = message.body.build()

        return@async ClientAppList(
            bytesAvailable = response.bytesAvailable,
            apps = response.appsList.map { app ->
                ClientAppListAppData(
                    appid = app.appid,
                    app = app.app,
                    category = app.category,
                    appType = app.appType,
                    numDownloading = app.numDownloading,
                    bytesDownloadRate = app.bytesDownloadRate,
                    bytesDownloaded = app.bytesDownloaded,
                    bytesToDownload = app.bytesToDownload,
                    dlcs = app.dlcsList.map { dlc ->
                        ClientAppListDlcData(
                            appId = dlc.appid,
                            app = dlc.app,
                            installed = dlc.installed,
                        )
                    },
                    favorite = app.favorite,
                    autoUpdate = app.autoUpdate,
                    installed = app.installed,
                    downloadPaused = app.downloadPaused,
                    changing = app.changing,
                    availableOnPlatform = app.availableOnPlatform,
                    bytesStaged = app.bytesStaged,
                    bytesToStage = app.bytesToStage,
                    bytesRequired = app.bytesRequired,
                    sourceBuildId = app.sourceBuildid,
                    targetBuildId = app.targetBuildid,
                    estimatedSecondsRemaining = app.estimatedSecondsRemaining,
                    queuePosition = app.queuePosition,
                    uninstalling = app.uninstalling,
                    rtTimeScheduled = app.rtTimeScheduled,
                    running = app.running,
                    updatePercentage = app.updatePercentage,
                )
            },
            clientInfo = ClientInfo(
                packageVersion = response.clientInfo.packageVersion,
                os = response.clientInfo.os,
                machineName = response.clientInfo.machineName,
                ipPublic = response.clientInfo.ipPublic,
                ipPrivate = response.clientInfo.ipPrivate,
                bytesAvailable = response.clientInfo.bytesAvailable,
                runningGames = response.clientInfo.runningGamesList.map { game ->
                    RunningGames(
                        appId = game.appid,
                        extraInfo = game.extraInfo,
                        timeRunningSec = game.timeRunningSec,
                    )
                },
                protocolVersion = response.clientInfo.protocolVersion,
                clientCommVersion = response.clientInfo.clientcommVersion,
                localUsers = response.clientInfo.localUsersList,
            ),
            refetchIntervalSecFull = response.refetchIntervalSecFull,
            refetchIntervalSecChanging = response.refetchIntervalSecChanging,
            refetchIntervalSecUpdating = response.refetchIntervalSecUpdating,
        )
    }

    /**
     * Adds the application to the remote installation queue.
     * @param remoteId The remote session ID of the client to query.
     * @param appId Application ID to install.
     * @return **true** if successful, otherwise false.
     */
    fun installClientApp(remoteId: Long, appId: Int): Deferred<Boolean> = client.defaultScope.async {
        val request = CClientComm_InstallClientApp_Request.newBuilder().apply {
            this.clientInstanceid = remoteId
            this.appid = appId
        }.build()

        val message = clientComm.installClientApp(request).await()
        // Request has empty response.

        return@async message.result == EResult.OK
    }

    /**
     * Sets the update state of an app in remote installation queue.
     * Action set to true will move the requested app to the top of the queue.
     * @param remoteId The remote session ID of the client to query
     * @param appId Application ID to update.
     * @param action true to prioritize (move to top of queue), false otherwise.
     * @return **true** if successful, otherwise false.
     */
    @JvmOverloads
    fun setClientAppUpdateState(
        remoteId: Long,
        appId: Int,
        action: Boolean = false,
    ): Deferred<Boolean> =
        client.defaultScope.async {
            val request = CClientComm_SetClientAppUpdateState_Request.newBuilder().apply {
                this.clientInstanceid = remoteId
                this.appid = appId
                this.action = if (action) 1 else 0
            }.build()

            val message = clientComm.setClientAppUpdateState(request).await()
            // Request has empty response.

            return@async message.result == EResult.OK
        }

    /**
     * Requests to uninstall the app from the device.
     * @param remoteId The remote session ID of the client to query.
     * @param appId Application ID to uninstall.
     * @return **true** if successful, otherwise false.
     */
    fun uninstallClientApp(
        remoteId: Long,
        appId: Int,
    ): Deferred<Boolean> = client.defaultScope.async {
        val request = CClientComm_UninstallClientApp_Request.newBuilder().apply {
            this.clientInstanceid = remoteId
            this.appid = appId
        }.build()

        val message = clientComm.uninstallClientApp(request).await()
        // Request has empty response.

        return@async message.result == EResult.OK
    }

    /**
     * Pauses or resumes downloads on the remote client.
     * @param remoteId The remote session ID of the client to query.
     * @param enable true to resume downloads, false to pause.
     * @return **true** if successful, otherwise false.
     */
    fun enableOrDisableDownloads(
        remoteId: Long,
        enable: Boolean,
    ): Deferred<Boolean> = client.defaultScope.async {
        val request = CClientComm_EnableOrDisableDownloads_Request.newBuilder().apply {
            this.clientInstanceid = remoteId
            this.enable = enable
        }.build()

        val message = clientComm.enableOrDisableDownloads(request).await()
        // Request has empty response.

        return@async message.result == EResult.OK
    }

    /**
     * Launches the application on the remote device.
     * @param remoteId The remote session ID of the client to query.
     * @param appId application ID to launch.
     * @param parameters Optional launch parameters/query string (e.g., command line arguments).
     * @return **true** if successful, otherwise false.
     */
    @JvmOverloads
    fun launchClientApp(
        remoteId: Long,
        appId: Int,
        parameters: String? = null,
    ): Deferred<Boolean> = client.defaultScope.async {
        val request = CClientComm_LaunchClientApp_Request.newBuilder().apply {
            this.clientInstanceid = remoteId
            this.appid = appId
            this.queryParams = parameters.orEmpty()
        }.build()

        val message = clientComm.launchClientApp(request).await()
        // Request has empty response.

        return@async message.result == EResult.OK
    }

    /**
     * Retrieves logon information for a specific Steam client.
     * @param remoteId The remote session ID of the client to query.
     * @return Client logon information including protocol version, OS, and machine name.
     */
    fun getClientLogonInfo(remoteId: Long): Deferred<ClientLogonInfo> = client.defaultScope.async {
        val request = CClientComm_GetClientLogonInfo_Request.newBuilder().apply {
            this.clientInstanceid = remoteId
        }.build()

        val message = clientComm.getClientLogonInfo(request).await()
        val response = message.body.build()

        return@async ClientLogonInfo(
            protocolVersion = response.protocolVersion,
            os = response.os,
            machineName = response.machineName
        )
    }

    /**
     * Retrieves detailed information about a specific Steam client, including system info and running games.
     * @param remoteId The remote session ID of the client to query.
     * @return Detailed client information including hardware, network, and active games.
     */
    fun getClientInfo(remoteId: Long): Deferred<ClientInfo> = client.defaultScope.async {
        val request = CClientComm_GetClientInfo_Request.newBuilder().apply {
            this.clientInstanceid = remoteId
        }.build()

        val message = clientComm.getClientInfo(request).await()
        val response = message.body.build()

        return@async ClientInfo(
            packageVersion = response.clientInfo.packageVersion,
            os = response.clientInfo.os,
            machineName = response.clientInfo.machineName,
            ipPublic = response.clientInfo.ipPublic,
            ipPrivate = response.clientInfo.ipPrivate,
            bytesAvailable = response.clientInfo.bytesAvailable,
            runningGames = response.clientInfo.runningGamesList.map { game ->
                RunningGames(
                    appId = game.appid,
                    extraInfo = game.extraInfo,
                    timeRunningSec = game.timeRunningSec,
                )
            },
            protocolVersion = response.clientInfo.protocolVersion,
            clientCommVersion = response.clientInfo.clientcommVersion,
            localUsers = response.clientInfo.localUsersList,
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
