package `in`.dragonbra.javasteam.steam.handlers.steamcloud.callback

import `in`.dragonbra.javasteam.enums.EResult
import `in`.dragonbra.javasteam.protobufs.steamclient.SteammessagesCloudSteamclient.CCloud_GetFileDetails_Response
import `in`.dragonbra.javasteam.steam.handlers.steamcloud.SteamCloud
import `in`.dragonbra.javasteam.steam.handlers.steamunifiedmessages.callback.ServiceMethodResponse
import `in`.dragonbra.javasteam.types.SteamID
import java.util.Date

/**
 * This callback is received in response to calling [SteamCloud.requestUGCDetails].
 */
@Suppress("MemberVisibilityCanBePrivate")
class UGCDetailsCallback(response: ServiceMethodResponse<CCloud_GetFileDetails_Response.Builder>) {

    /**
     * Gets the result of the request.
     */
    val result: EResult = response.result

    /**
     * Gets the App ID the UGC is for.
     */
    val appID: Int

    /**
     * Gets the UGC ID.
     */
    val ugcID: Long

    /**
     * Gets the SteamID of the UGC's creator.
     */
    val creator: SteamID

    /**
     * Gets the URL that the content is located at.
     */
    val url: String

    /**
     * Gets the name of the file.
     */
    val fileName: String

    /**
     * Gets the size of the file.
     */
    val fileSize: Int

    /**
     * Gets the timestamp of the file.
     */
    val timestamp: Date

    /**
     * Gets the SHA hash of the file.
     */
    val fileSHA: String

    /**
     * Gets the compressed size of the file.
     */
    val compressedFileSize: Int

    /**
     * Gets the rangecheck host.
     */
    val rangecheckHost: String

    init {
        val msg = response.body.details

        appID = msg.appid
        ugcID = msg.ugcid
        creator = SteamID(msg.steamidCreator)

        url = msg.url

        fileName = msg.filename
        fileSize = msg.fileSize
        timestamp = Date(msg.timestamp * 1000L)
        fileSHA = msg.fileSha
        compressedFileSize = msg.compressedFileSize

        rangecheckHost = response.body.rangecheckHost
    }
}
