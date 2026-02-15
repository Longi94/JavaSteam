package `in`.dragonbra.javasteam.steam.handlers.steamcloud.callback

import `in`.dragonbra.javasteam.enums.EResult
import `in`.dragonbra.javasteam.protobufs.steamclient.SteammessagesCloudSteamclient.CCloud_GetSingleFileInfo_Response
import `in`.dragonbra.javasteam.steam.handlers.steamcloud.SteamCloud
import `in`.dragonbra.javasteam.steam.handlers.steamunifiedmessages.callback.ServiceMethodResponse
import java.util.*

/**
 * This callback is received in response to calling [SteamCloud.getSingleFileInfo].
 */
@Suppress("MemberVisibilityCanBePrivate")
class SingleFileInfoCallback(response: ServiceMethodResponse<CCloud_GetSingleFileInfo_Response.Builder>) {

    /**
     * Gets the result of the request.
     */
    val result: EResult = response.result

    /**
     * Gets the App ID the file is for.
     */
    val appID: Int

    /**
     * Gets the file name request.
     */
    val fileName: String

    /**
     * Gets the SHA hash of the file.
     */
    val shaHash: ByteArray

    /**
     * Gets the timestamp of the file.
     */
    val timestamp: Date

    /**
     * Gets the size of the file.
     */
    val fileSize: Int

    /**
     * Gets if the file was explicitly deleted by the user.
     */
    val isExplicitDelete: Boolean

    init {
        val msg = response.body

        appID = msg.appId
        fileName = msg.fileName
        shaHash = msg.shaFile.toByteArray()
        timestamp = Date(msg.timeStamp * 1000L)
        fileSize = msg.rawFileSize
        isExplicitDelete = msg.isExplicitDelete
    }
}
