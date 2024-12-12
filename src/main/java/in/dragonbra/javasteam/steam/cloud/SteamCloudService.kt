package `in`.dragonbra.javasteam.steam.cloud

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

    internal val cloudService: Cloud

    init {
        val unifiedMessages = steamClient.getHandler(SteamUnifiedMessages::class.java)
            ?: throw NullPointerException("Unable to get SteamUnifiedMessages handler")

        cloudService = unifiedMessages.createService<Cloud>()
    }

    /**
     * Retrieve the file list change for the user files of a certain app/game since
     * the last sync change.
     *
     * @param appId the ID of the app/game whose user files to check
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
}
