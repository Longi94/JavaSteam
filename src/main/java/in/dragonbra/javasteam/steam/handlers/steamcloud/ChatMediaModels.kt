package `in`.dragonbra.javasteam.steam.handlers.steamcloud

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class BeginFileUploadResponse(
    val result: BeginFileUploadResult,
    val hmac: String,
)

@Serializable
internal data class BeginFileUploadResult(
    @SerialName("use_https") val useHttps: Boolean,
    @SerialName("url_host") val urlHost: String,
    @SerialName("url_path") val urlPath: String,
    @SerialName("request_headers") val requestHeaders: List<CdnRequestHeader>,
    val ugcid: String,
    val timestamp: String,
)

@Serializable
internal data class CdnRequestHeader(
    val name: String,
    val value: String,
)
