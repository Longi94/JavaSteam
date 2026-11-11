package `in`.dragonbra.javasteam.steam.handlers.steamcloud

import kotlinx.coroutines.runBlocking
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import mockwebserver3.RecordedRequest
import okhttp3.OkHttpClient
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.IOException
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import java.security.MessageDigest

class ChatMediaUploaderTest {

    private lateinit var server: MockWebServer

    private lateinit var uploader: ChatMediaUploader

    @BeforeEach
    fun setUp() {
        server = MockWebServer()
        server.start()

        uploader = ChatMediaUploader(OkHttpClient(), server.url("/"))
    }

    @AfterEach
    fun tearDown() {
        server.close()
    }

    @Test
    fun uploadsToAFriendInThreeRequests() {
        server.enqueue(beginResponse())
        server.enqueue(MockResponse.Builder().build())
        server.enqueue(MockResponse.Builder().body("{}").build())

        val ugcId = upload(media = PNG, fileName = "cat.png", spoiler = true)

        assertEquals(UGC_ID, ugcId)
        assertEquals(3, server.requestCount)

        // Ask where to upload to
        val begin = server.takeRequest()
        val beginFields = begin.formFields()
        assertEquals("POST", begin.method)
        assertEquals("/chat/beginfileupload", begin.url.encodedPath)
        assertEquals("cat.png", beginFields["file_name"])
        assertEquals(PNG.size.toString(), beginFields["file_size"])
        assertEquals(sha1(PNG), beginFields["file_sha"])
        assertEquals("image/png", beginFields["file_type"])
        assertEquals("640", beginFields["file_image_width"])
        assertEquals("480", beginFields["file_image_height"])

        // The login cookie is the SteamID and token, url encoded. The session cookie matches the form field.
        val cookies = begin.cookies()
        assertEquals("$STEAM_ID%7C%7C$ACCESS_TOKEN", cookies["steamLoginSecure"])
        assertEquals(beginFields["sessionid"], cookies["sessionid"])
        assertTrue(cookies["sessionid"]!!.matches(Regex("[0-9a-f]{32}")))

        // Upload the file, with the headers Steam handed out and without our cookies
        val put = server.takeRequest()
        assertEquals("PUT", put.method)
        assertEquals("/ugcupload/abc", put.url.encodedPath)
        assertEquals("yes", put.headers["x-upload-authorized"])
        assertNull(put.headers["Cookie"])
        assertArrayEquals(PNG, put.body!!.toByteArray())

        // Commit it to the chat
        val commit = server.takeRequest()
        val commitFields = commit.formFields()
        assertEquals("POST", commit.method)
        assertEquals("/chat/commitfileupload", commit.url.encodedPath)
        assertEquals(cookies, commit.cookies())
        assertEquals("1", commitFields["success"])
        assertEquals(UGC_ID.toString(), commitFields["ugcid"])
        assertEquals("1700000000", commitFields["timestamp"])
        assertEquals("deadbeef", commitFields["hmac"])
        assertEquals("1", commitFields["spoiler"])
        assertEquals("76561198000000001", commitFields["friend_steamid"])

        // Everything from the first request is sent again
        beginFields.forEach { (name, value) -> assertEquals(value, commitFields[name], name) }
    }

    @Test
    fun makesUpAFileNameFromTheType() {
        server.enqueue(beginResponse())
        server.enqueue(MockResponse.Builder().build())
        server.enqueue(MockResponse.Builder().body("{}").build())

        upload(media = GIF, fileName = null)

        val fields = server.takeRequest().formFields()
        assertTrue(fields["file_name"]!!.matches(Regex("\\d+_media\\.gif")), fields["file_name"])
        assertEquals("image/gif", fields["file_type"])

        // Skip the upload itself, then check the commit
        server.takeRequest()
        assertEquals("0", server.takeRequest().formFields()["spoiler"])
    }

    @Test
    fun uploadsAVideoWithoutASize() {
        server.enqueue(beginResponse())
        server.enqueue(MockResponse.Builder().build())
        server.enqueue(MockResponse.Builder().body("{}").build())

        upload(media = byteArrayOf(0x1A, 0x45, 0xDF.toByte(), 0xA3.toByte(), 0x01, 0x02, 0x03, 0x04))

        val fields = server.takeRequest().formFields()
        assertEquals("video/webm", fields["file_type"])
        assertEquals("0", fields["file_image_width"])
        assertEquals("0", fields["file_image_height"])
        assertTrue(fields["file_name"]!!.endsWith("_media.webm"), fields["file_name"])
    }

    @Test
    fun rejectsUnsupportedMedia() {
        assertThrows(IllegalArgumentException::class.java) { upload(media = "not an image".toByteArray()) }
        assertThrows(IllegalArgumentException::class.java) { upload(media = ByteArray(0)) }

        assertEquals(0, server.requestCount)
    }

    @Test
    fun rejectsMediaOverTheSizeLimit() {
        val tooLarge = PNG.copyOf(ChatMediaUploader.MAX_SIZE + 1)

        assertThrows(IllegalArgumentException::class.java) { upload(media = tooLarge) }

        assertEquals(0, server.requestCount)
    }

    @Test
    fun failsWhenTheTokenIsRejected() {
        server.enqueue(MockResponse.Builder().code(401).build())

        assertThrows(IOException::class.java) { upload() }

        assertEquals(1, server.requestCount)
    }

    @Test
    fun failsWhenSteamAnswersWithALoginPage() {
        server.enqueue(MockResponse.Builder().body("<html>Sign In</html>").build())

        assertThrows(IOException::class.java) { upload() }

        assertEquals(1, server.requestCount)
    }

    @Test
    fun failsWhenTheReplyHasNoUploadTarget() {
        server.enqueue(MockResponse.Builder().body("""{"success":false}""").build())

        assertThrows(IOException::class.java) { upload() }

        assertEquals(1, server.requestCount)
    }

    @Test
    fun doesNotCommitWhenTheUploadFails() {
        server.enqueue(beginResponse())
        server.enqueue(MockResponse.Builder().code(500).build())

        assertThrows(IOException::class.java) { upload() }

        assertEquals(2, server.requestCount)
    }

    @Test
    fun failsWhenTheCommitFails() {
        server.enqueue(beginResponse())
        server.enqueue(MockResponse.Builder().build())
        server.enqueue(MockResponse.Builder().code(400).build())

        assertThrows(IOException::class.java) { upload() }
    }

    private fun upload(media: ByteArray = PNG, fileName: String? = null, spoiler: Boolean = false): Long = runBlocking {
        uploader.upload(
            steamId = STEAM_ID,
            accessToken = ACCESS_TOKEN,
            media = media,
            fileName = fileName,
            spoiler = spoiler,
            destination = mapOf("friend_steamid" to "76561198000000001"),
        )
    }

    // Steam mixes quoted and bare values, and sends fields that aren't needed
    private fun beginResponse(): MockResponse = MockResponse.Builder().body(
        """
        {
            "success": 1,
            "result": {
                "ugcid": "$UGC_ID",
                "timestamp": 1700000000,
                "use_https": false,
                "url_host": "${server.hostName}:${server.port}",
                "url_path": "/ugcupload/abc",
                "request_headers": [
                    { "name": "x-upload-authorized", "value": "yes" }
                ]
            },
            "hmac": "deadbeef"
        }
        """.trimIndent()
    ).build()

    private fun RecordedRequest.formFields(): Map<String, String> = body!!.utf8().split('&').associate {
        val (name, value) = it.split('=', limit = 2)
        URLDecoder.decode(name, StandardCharsets.UTF_8) to URLDecoder.decode(value, StandardCharsets.UTF_8)
    }

    private fun RecordedRequest.cookies(): Map<String, String> = headers["Cookie"]!!.split("; ").associate {
        val (name, value) = it.split('=', limit = 2)
        name to value
    }

    private fun sha1(data: ByteArray): String =
        MessageDigest.getInstance("SHA-1").digest(data).joinToString("") { "%02x".format(it) }

    private companion object {
        const val STEAM_ID = 76561198000000000L

        const val ACCESS_TOKEN = "header.payload.signature"

        const val UGC_ID = 2456789012345678901L

        // Just the headers, which is all that gets read
        val PNG: ByteArray = ChatMediaInfoTest.png(640, 480)

        val GIF: ByteArray = ChatMediaInfoTest.gif(32, 16)
    }
}
