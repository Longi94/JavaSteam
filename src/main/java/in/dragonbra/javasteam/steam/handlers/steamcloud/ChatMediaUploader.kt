package `in`.dragonbra.javasteam.steam.handlers.steamcloud

import `in`.dragonbra.javasteam.util.JavaSteamAddition
import `in`.dragonbra.javasteam.util.Strings
import `in`.dragonbra.javasteam.util.Versions
import `in`.dragonbra.javasteam.util.log.LogManager
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import okhttp3.FormBody
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.coroutines.executeAsync
import java.io.IOException
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.security.SecureRandom

/**
 * Uploads an image or video to a Steam chat through the community website, the way the web and mobile chat do.
 * It takes three requests: ask where to upload, PUT the file there, then commit it to a chat.
 * Steam posts the media to the chat itself once it is committed.
 *
 * Referenced from steam.py: https://github.com/Gobot1234/steam.py/blob/main/steam/http.py
 *
 * @param httpClient The client to make the requests with.
 * @param communityUrl The base address of the community website.
 */
@JavaSteamAddition
internal class ChatMediaUploader(
    private val httpClient: OkHttpClient,
    private val communityUrl: HttpUrl = COMMUNITY_URL,
) {

    /**
     * @param steamId The 64-bit SteamID of the logged-in user.
     * @param accessToken The access token of the logged-in user.
     * @param media The file, a PNG, JPEG or GIF image, or a WebM, MPEG or OGV video.
     * @param fileName The file name, or null to make one up.
     * @param spoiler Whether the media is hidden until clicked.
     * @param destination The form fields that pick the chat to post to.
     * @return Where Steam stored the media.
     * @throws IllegalArgumentException if the media is too large or not a supported format.
     * @throws IOException if a request fails, Steam turns it down, or its reply can't be understood.
     */
    suspend fun upload(
        steamId: Long,
        accessToken: String,
        media: ByteArray,
        fileName: String?,
        spoiler: Boolean,
        destination: Map<String, String>,
    ): ChatMediaUploadResult {
        require(media.isNotEmpty()) { "The media is empty" }
        require(media.size <= MAX_SIZE) { "The media is too large to upload, the limit is 10 MiB" }

        val info = ChatMediaInfo.detect(media)
        val name = fileName ?: "${System.currentTimeMillis() / 1000}_media.${info.extension}"

        // The website only checks that the sessionid cookie and form field match, so any value will do
        val sessionId = Strings.toHex(ByteArray(16).also(SecureRandom()::nextBytes)).lowercase()
        val loginSecure = URLEncoder.encode("${steamId.toULong()}||$accessToken", StandardCharsets.UTF_8)
        val cookies = "steamLoginSecure=$loginSecure; sessionid=$sessionId"

        val fileFields = mapOf(
            "sessionid" to sessionId,
            "l" to "english",
            "file_size" to media.size.toString(),
            "file_name" to name,
            "file_sha" to Strings.toHex(MessageDigest.getInstance("SHA-1").digest(media)).lowercase(),
            "file_image_width" to info.width.toString(),
            "file_image_height" to info.height.toString(),
            "file_type" to info.mimeType,
        )

        // Ask where to upload to
        val target = parseBeginResponse(post("chat/beginfileupload", cookies, fileFields))

        // Upload the file there. The headers Steam handed out authorize it, the cookies stay home.
        val putRequest = Request.Builder()
            .url(target.url)
            .header("User-Agent", USER_AGENT)
            .apply { target.headers.forEach { (name, value) -> header(name, value) } }
            .put(media.toRequestBody())
            .build()

        logger.debug("Uploading ${media.size} bytes to ${putRequest.url.host}")

        httpClient.newCall(putRequest).executeAsync().use { response ->
            logger.debug("Upload answered HTTP ${response.code}")

            if (!response.isSuccessful) {
                throw IOException("Uploading the media failed: HTTP ${response.code}")
            }
        }

        // Commit it, which posts it to the chat
        val commitFields = fileFields + mapOf(
            "success" to "1",
            "ugcid" to target.ugcId,
            "timestamp" to target.timestamp,
            "hmac" to target.hmac,
            "spoiler" to if (spoiler) "1" else "0",
        ) + destination

        val commit = parseObject("commitfileupload", post("chat/commitfileupload", cookies, commitFields))

        // The reply repeats what was stored and adds the address it ended up at.
        // Its own ugcid is null, so the one from the first request is the one to keep.
        val details = (commit["result"] as? JsonObject)?.get("details") as? JsonObject

        return ChatMediaUploadResult(
            ugcID = target.ugcId.toULongOrNull()?.toLong()
                ?: throw IOException("beginfileupload returned an invalid ugcid: ${target.ugcId}"),
            url = details?.stringOrNull("url").orEmpty(),
            fileName = details?.stringOrNull("filename") ?: name,
            fileSize = media.size,
            fileSHA = fileFields.getValue("file_sha"),
        )
    }

    private suspend fun post(path: String, cookies: String, fields: Map<String, String>): String {
        val body = FormBody.Builder()
            .apply { fields.forEach { (name, value) -> add(name, value) } }
            .build()

        val request = Request.Builder()
            .url(communityUrl.newBuilder().addPathSegments(path).build())
            .header("User-Agent", USER_AGENT)
            .header("Cookie", cookies)
            .post(body)
            .build()

        return httpClient.newCall(request).executeAsync().use { response ->
            logger.debug("$path answered HTTP ${response.code}")

            if (!response.isSuccessful) {
                throw IOException("${path.substringAfterLast('/')} failed: HTTP ${response.code}")
            }

            response.body.string()
        }
    }

    private class UploadTarget(
        val url: String,
        val headers: List<Pair<String, String>>,
        val ugcId: String,
        val timestamp: String,
        val hmac: String,
    )

    private fun parseBeginResponse(json: String): UploadTarget {
        val root = parseObject("beginfileupload", json)

        try {
            val result = root["result"]?.jsonObject
                ?: throw IOException("beginfileupload returned no upload target, is the access token valid?")

            val scheme = if (result.string("use_https") in TRUTHY) "https" else "http"

            val headers = result["request_headers"]?.jsonArray.orEmpty().map { header ->
                header.jsonObject.string("name") to header.jsonObject.string("value")
            }

            return UploadTarget(
                url = "$scheme://${result.string("url_host")}${result.string("url_path")}",
                headers = headers,
                ugcId = result.string("ugcid"),
                timestamp = result.string("timestamp"),
                hmac = root.string("hmac"),
            )
        } catch (e: IllegalArgumentException) {
            throw IOException("beginfileupload returned an unexpected response", e)
        }
    }

    /**
     * Parses a reply of the website, which has to be a JSON object that doesn't report a failure.
     */
    private fun parseObject(name: String, json: String): JsonObject {
        val root = try {
            Json.parseToJsonElement(json).jsonObject
        } catch (e: IllegalArgumentException) {
            // Not JSON. A login page comes back when the token is rejected.
            throw IOException("$name returned an unexpected response, is the access token valid?", e)
        }

        // 1 when all is well, anything else is the reason it isn't
        val success = root.stringOrNull("success")
        if (success != null && success !in TRUTHY) {
            throw IOException("$name was turned down by Steam: success=$success")
        }

        return root
    }

    private fun JsonObject.stringOrNull(key: String): String? =
        (this[key] as? JsonPrimitive)?.takeUnless { it is JsonNull }?.content

    private fun JsonObject.string(key: String): String = stringOrNull(key)
        ?: throw IOException("beginfileupload response is missing '$key'")

    companion object {
        private val logger = LogManager.getLogger(ChatMediaUploader::class.java)

        val COMMUNITY_URL: HttpUrl = "https://steamcommunity.com/".toHttpUrl()

        const val MAX_SIZE: Int = 10 * 1024 * 1024

        private val USER_AGENT = "JavaSteam-${Versions.getVersion()}"

        private val TRUTHY = setOf("true", "1")
    }
}
