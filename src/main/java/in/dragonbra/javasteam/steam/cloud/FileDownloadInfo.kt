package `in`.dragonbra.javasteam.steam.cloud

import `in`.dragonbra.javasteam.protobufs.steamclient.SteammessagesCloudSteamclient.CCloud_ClientFileDownload_Response
import java.util.Date

class FileDownloadInfo(response: CCloud_ClientFileDownload_Response.Builder) {
    data class HttpHeaders(
        val name: String,
        val value: String
    )

    val appID: Int = response.appid
    val fileSize: Int = response.fileSize
    val rawFileSize: Int = response.rawFileSize
    val shaFile: ByteArray = response.shaFile.toByteArray()
    val timestamp: Date = Date(response.timeStamp * 1000L)
    val isExplicitDelete: Boolean = response.isExplicitDelete
    val urlHost: String = response.urlHost
    val urlPath: String = response.urlPath
    val useHttps: Boolean = response.useHttps
    val requestHeaders: List<HttpHeaders> = response.requestHeadersList.map { HttpHeaders(it.name, it.value) }
    val encrypted: Boolean = response.encrypted
}
