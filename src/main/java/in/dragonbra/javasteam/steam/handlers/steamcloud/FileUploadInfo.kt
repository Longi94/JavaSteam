package `in`.dragonbra.javasteam.steam.handlers.steamcloud

import `in`.dragonbra.javasteam.protobufs.steamclient.SteammessagesCloudSteamclient.CCloud_ClientBeginFileUpload_Response

class FileUploadInfo(response: CCloud_ClientBeginFileUpload_Response.Builder) {
    val encryptFile = response.encryptFile
    val blockRequests = response.blockRequestsList.map { FileUploadBlockDetails(it) }
}
