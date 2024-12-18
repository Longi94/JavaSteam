package `in`.dragonbra.javasteam.steam.handlers.steamcloud

import `in`.dragonbra.javasteam.protobufs.steamclient.SteammessagesCloudSteamclient.ClientCloudFileUploadBlockDetails

class FileUploadBlockDetails(blockDetails: ClientCloudFileUploadBlockDetails) {
    val urlHost: String = blockDetails.urlHost
    val urlPath: String = blockDetails.urlPath
    val useHttps: Boolean = blockDetails.useHttps
    val httpMethod: Int = blockDetails.httpMethod
    val requestHeaders: List<HttpHeaders> = blockDetails.requestHeadersList.map { HttpHeaders(it.name, it.value) }
    val blockOffset: Long = blockDetails.blockOffset
    val blockLength: Int = blockDetails.blockLength
    val explicitBodyData: ByteArray = blockDetails.explicitBodyData.toByteArray()
    val mayParallelize: Boolean = blockDetails.mayParallelize
}
