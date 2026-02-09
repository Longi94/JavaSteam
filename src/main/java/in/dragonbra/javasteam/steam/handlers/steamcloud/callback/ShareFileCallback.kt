package `in`.dragonbra.javasteam.steam.handlers.steamcloud.callback

import `in`.dragonbra.javasteam.enums.EResult
import `in`.dragonbra.javasteam.protobufs.steamclient.SteammessagesCloudSteamclient.CCloud_ShareFile_Response
import `in`.dragonbra.javasteam.steam.handlers.steamcloud.SteamCloud
import `in`.dragonbra.javasteam.steam.handlers.steamunifiedmessages.callback.ServiceMethodResponse

/**
 * This callback is received in response to calling [SteamCloud.shareFile].
 */
class ShareFileCallback(response: ServiceMethodResponse<CCloud_ShareFile_Response.Builder>) {

    /**
     * Gets the result of the request.
     */
    val result: EResult = response.result

    /**
     * Gets the resulting UGC handle.
     */
    val ugcId: Long

    init {
        val msg = response.body

        ugcId = msg.hcontent
    }
}
