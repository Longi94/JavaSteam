package `in`.dragonbra.javasteam.steam.handlers.steamclientcommunication

import `in`.dragonbra.javasteam.base.IPacketMsg
import `in`.dragonbra.javasteam.enums.EResult
import `in`.dragonbra.javasteam.protobufs.webui.ServiceClientcomm.CClientComm_EnableOrDisableDownloads_Request
import `in`.dragonbra.javasteam.protobufs.webui.ServiceClientcomm.CClientComm_GetAllClientLogonInfo_Request
import `in`.dragonbra.javasteam.protobufs.webui.ServiceClientcomm.CClientComm_GetClientAppList_Request
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
@JavaSteamAddition
class SteamClientCommunication : ClientMsgHandler() {

    private val clientComm: ClientComm by lazy {
        val unifiedMessages = client.getHandler<SteamUnifiedMessages>()
            ?: throw NullPointerException("Unable to get SteamUnifiedMessages handler")
        unifiedMessages.createService<ClientComm>()
    }

    /**
     * Return the list of active devices that are running any Steam client and connected to the network.
     * Note: this will return all connected clients. Filter the results based on OS or device type.
     */
    fun getAllClientLogonInfo(): Deferred<ClientLogonInfo> = client.defaultScope.async {
        val request = CClientComm_GetAllClientLogonInfo_Request.newBuilder().build()

        val message = clientComm.getAllClientLogonInfo(request).await()
        val response = message.body.build()

        return@async ClientLogonInfo(
            sessions = response.sessionsList.map {
                ClientLogonInfoSession(
                    clientInstanceId = it.clientInstanceid,
                    protocolVersion = it.protocolVersion,
                    osName = it.osName,
                    machineName = it.machineName,
                    osType = it.osType,
                    deviceType = it.deviceType,
                    realm = it.realm,
                )
            },
            refetchIntervalSec = response.refetchIntervalSec,
        )
    }

    /**
     * Return the list of applications of the remote device.
     * This is not the list of downloaded apps, but the whole "available" list with a flag indicating about it being downloaded.
     * @param remoteId remote session ID
     * @param filters filters
     * @param language language
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
     * @param remoteId
     * @param appId
     * @return **true** if successfully, otherwise false.
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
     * @param remoteId
     * @param appId
     * @param action
     * @return **true** if successfully, otherwise false.
     */
    fun setClientAppUpdateState(remoteId: Long, appId: Int, action: Boolean): Deferred<Boolean> =
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
     * @param remoteId
     * @param appId
     * @return **true** if successfully, otherwise false.
     */
    fun uninstallClientApp(remoteId: Long, appId: Int): Deferred<Boolean> = client.defaultScope.async {
        val request = CClientComm_UninstallClientApp_Request.newBuilder().apply {
            this.clientInstanceid = remoteId
            this.appid = appId
        }.build()

        val message = clientComm.uninstallClientApp(request).await()
        // Request has empty response.

        return@async message.result == EResult.OK
    }

    /**
     * Pauses or resumes active download - the first item in the queue.
     * @param remoteId
     * @param enable
     * @return **true** if successfully, otherwise false.
     */
    fun enableOrDisableDownloads(remoteId: Long, enable: Boolean): Deferred<Boolean> = client.defaultScope.async {
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
     * @param remoteId
     * @param appId
     * @param parameters
     * @return **true** if successfully, otherwise false.
     */
    fun launchClientApp(remoteId: Long, appId: Int, parameters: String? = null): Deferred<Boolean> =
        client.defaultScope.async {
            val request = CClientComm_LaunchClientApp_Request.newBuilder().apply {
                this.clientInstanceid = remoteId
                this.appid = appId
                this.queryParams = parameters.orEmpty()
            }.build()

            val message = clientComm.launchClientApp(request).await()
            // Request has empty response.

            return@async message.result == EResult.OK
        }

    // TODO: GetClientLogonInfo()

    // TODO: GetClientInfo()

    /**
     * Handles a client message. This should not be called directly.
     *
     * @param packetMsg The packet message that contains the data.
     */
    override fun handleMsg(packetMsg: IPacketMsg) {
        // not used
    }
}
