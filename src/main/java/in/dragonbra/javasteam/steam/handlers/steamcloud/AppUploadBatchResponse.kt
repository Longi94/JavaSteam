package `in`.dragonbra.javasteam.steam.handlers.steamcloud

import `in`.dragonbra.javasteam.protobufs.steamclient.SteammessagesCloudSteamclient.CCloud_BeginAppUploadBatch_Response

@Suppress("unused")
class AppUploadBatchResponse(response: CCloud_BeginAppUploadBatch_Response.Builder) {
    val batchID: Long = response.batchId
    val appChangeNumber: Long = response.appChangeNumber
}
