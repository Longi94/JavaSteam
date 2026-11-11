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
        enqueueUpload(fileName = "cat.png")

        upload(media = PNG, fileName = "cat.png", spoiler = true)

        assertEquals(3, server.requestCount)

        // Ask where to upload to
        val begin = server.takeRequest()
        val beginFields = begin.formFields()
        assertEquals("POST", begin.method)
        assertEquals("/chat/beginfileupload", begin.url.encodedPath)
        assertEquals("english", beginFields["l"])
        assertEquals("cat.png", beginFields["file_name"])
        assertEquals(PNG.size.toString(), beginFields["file_size"])
        assertEquals(sha1(PNG), beginFields["file_sha"])
        assertEquals("image/png", beginFields["file_type"])
        assertEquals("735", beginFields["file_image_width"])
        assertEquals("561", beginFields["file_image_height"])

        // The login cookie is the SteamID and token, url encoded. The session cookie matches the form field.
        val cookies = begin.cookies()
        assertEquals("$STEAM_ID%7C%7C$ACCESS_TOKEN", cookies["steamLoginSecure"])
        assertEquals(beginFields["sessionid"], cookies["sessionid"])
        assertTrue(cookies["sessionid"]!!.matches(Regex("[0-9a-f]{32}")))

        // Upload the file, with the headers Steam handed out and without our cookies
        val put = server.takeRequest()
        assertEquals("PUT", put.method)
        assertEquals(UPLOAD_PATH, put.url.encodedPath)
        assertEquals("image/png", put.headers["Content-Type"])
        assertEquals(PNG.size.toString(), put.headers["Content-Length"])
        assertEquals("inline; filename*=UTF-8''cat.png;", put.headers["Content-Disposition"])
        assertEquals("inline; filename*=UTF-8''cat.png;", put.headers["x-ms-blob-content-disposition"])
        assertEquals("SharedKey steamugctest:c2lnbmF0dXJl", put.headers["Authorization"])
        assertEquals("Wed, 11 Nov 2026 22:27:30 GMT", put.headers["x-ms-date"])
        assertEquals("2017-04-17", put.headers["x-ms-version"])
        assertEquals("BlockBlob", put.headers["x-ms-blob-type"])
        assertNull(put.headers["Cookie"])
        assertArrayEquals(PNG, put.body!!.toByteArray())

        // Commit it to the chat
        val commit = server.takeRequest()
        val commitFields = commit.formFields()
        assertEquals("POST", commit.method)
        assertEquals("/chat/commitfileupload", commit.url.encodedPath)
        assertEquals(cookies, commit.cookies())
        assertEquals("1", commitFields["success"])
        assertEquals(UGC_ID, commitFields["ugcid"])
        assertEquals(TIMESTAMP, commitFields["timestamp"])
        assertEquals(HMAC, commitFields["hmac"])
        assertEquals("1", commitFields["spoiler"])
        assertEquals(FRIEND, commitFields["friend_steamid"])

        // Everything from the first request is sent again
        beginFields.forEach { (name, value) -> assertEquals(value, commitFields[name], name) }
    }

    @Test
    fun returnsWhereSteamStoredTheMedia() {
        enqueueUpload(fileName = "cat.png")

        val result = upload(media = PNG, fileName = "cat.png")

        // The ID is over the signed maximum, so it reads as negative
        assertEquals(UGC_ID, result.ugcID.toULong().toString())
        assertEquals("https://images.steamusercontent.com/ugc/$UGC_ID/${sha1(PNG).uppercase()}/", result.url)
        assertEquals("cat.png", result.fileName)
        assertEquals(PNG.size, result.fileSize)
        assertEquals(sha1(PNG), result.fileSHA)
    }

    @Test
    fun returnsWhatItKnowsWhenTheCommitReplyHasNoDetails() {
        server.enqueue(beginResponse(fileName = "cat.png"))
        server.enqueue(MockResponse.Builder().code(201).build())
        server.enqueue(MockResponse.Builder().body("""{"success":1}""").build())

        val result = upload(media = PNG, fileName = "cat.png")

        assertEquals(UGC_ID, result.ugcID.toULong().toString())
        assertEquals("", result.url)
        assertEquals("cat.png", result.fileName)
        assertEquals(PNG.size, result.fileSize)
    }

    @Test
    fun makesUpAFileNameFromTheType() {
        enqueueUpload()

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
        enqueueUpload()

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

    // What a turned down request looks like is a guess, only successful replies have been seen
    @Test
    fun failsWhenSteamTurnsDownTheBegin() {
        server.enqueue(MockResponse.Builder().body("""{"success":15}""").build())

        val e = assertThrows(IOException::class.java) { upload() }

        assertTrue(e.message!!.contains("success=15"), e.message)
        assertEquals(1, server.requestCount)
    }

    @Test
    fun failsWhenTheReplyHasNoUploadTarget() {
        server.enqueue(MockResponse.Builder().body("""{"success":1}""").build())

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
        server.enqueue(MockResponse.Builder().code(201).build())
        server.enqueue(MockResponse.Builder().code(400).build())

        assertThrows(IOException::class.java) { upload() }
    }

    // As above, the shape of a turned down commit is a guess
    @Test
    fun failsWhenSteamTurnsDownTheCommit() {
        server.enqueue(beginResponse())
        server.enqueue(MockResponse.Builder().code(201).build())
        server.enqueue(MockResponse.Builder().body("""{"success":2}""").build())

        val e = assertThrows(IOException::class.java) { upload() }

        assertTrue(e.message!!.contains("success=2"), e.message)
    }

    private fun upload(
        media: ByteArray = PNG,
        fileName: String? = null,
        spoiler: Boolean = false,
    ): ChatMediaUploadResult = runBlocking {
        uploader.upload(
            steamId = STEAM_ID,
            accessToken = ACCESS_TOKEN,
            media = media,
            fileName = fileName,
            spoiler = spoiler,
            destination = mapOf("friend_steamid" to FRIEND),
        )
    }

    private fun enqueueUpload(fileName: String = "cat.png") {
        server.enqueue(beginResponse(fileName))
        // The storage answers a finished upload with a 201 and no body
        server.enqueue(MockResponse.Builder().code(201).build())
        server.enqueue(commitResponse(fileName))
    }

    // Steam's reply, down to the escaped slashes, the bare timestamp and the success that shows up twice.
    // The real one has use_https set to 1 and a storage host, here it has to point at the plain http test server.
    private fun beginResponse(fileName: String = "cat.png"): MockResponse = MockResponse.Builder()
        .addHeader("Content-Type", "application/json; charset=utf-8")
        .body(
            """{"success":1,"result":{"storage_system":1,"ugcid":"$UGC_ID","timestamp":$TIMESTAMP,""" +
                """"url_host":"${server.hostName}:${server.port}",""" +
                """"url_path":"${UPLOAD_PATH.replace("/", "\\/")}","use_https":0,""" +
                """"request_headers":[{"name":"Content-Type","value":"image\/png"},""" +
                """{"name":"Content-Length","value":"${PNG.size}"},""" +
                """{"name":"Content-Disposition","value":"inline; filename*=UTF-8''$fileName;"},""" +
                """{"name":"x-ms-blob-content-disposition","value":"inline; filename*=UTF-8''$fileName;"},""" +
                """{"name":"Authorization","value":"SharedKey steamugctest:c2lnbmF0dXJl"},""" +
                """{"name":"x-ms-date","value":"Wed, 11 Nov 2026 22:27:30 GMT"},""" +
                """{"name":"x-ms-version","value":"2017-04-17"},""" +
                """{"name":"x-ms-blob-type","value":"BlockBlob"}],"success":1,"rwgrsn":-2},""" +
                """"hmac":"$HMAC","timestamp":$TIMESTAMP}"""
        )
        .build()

    // Steam's reply. The ugcid is null in it, and the timestamp is quoted where the first reply had it bare.
    private fun commitResponse(fileName: String = "cat.png"): MockResponse = MockResponse.Builder()
        .addHeader("Content-Type", "application/json; charset=utf-8")
        .body(
            """{"success":1,"result":{"details":{"appid":744350,"ugcid":null,"filename":"$fileName",""" +
                """"timestamp":"$TIMESTAMP","file_size":${PNG.size},""" +
                """"url":"https:\/\/images.steamusercontent.com\/ugc\/$UGC_ID\/${sha1(PNG).uppercase()}\/",""" +
                """"steamid_creator":"$STEAM_ID","flags":16,"platforms_to_sync":[],""" +
                """"file_sha":"${sha1(PNG)}","compressed_file_size":${PNG.size}},""" +
                """"rangecheck_host":null,"success":1,"rwgrsn":-2}}"""
        )
        .build()

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

        const val FRIEND = "76561198000000001"

        const val ACCESS_TOKEN = "header.payload.signature"

        // Over the signed 64-bit maximum, like the IDs Steam hands out
        const val UGC_ID = "15000000000000000001"

        const val TIMESTAMP = "1794436050"

        const val HMAC = "0123456789abcdef0123456789abcdef"

        const val UPLOAD_PATH = "/cloud/AB/95/84/12/744350/372_5_0D289C3F_898ED_1.dat"

        // Just the headers, which is all that gets read
        val PNG: ByteArray = ChatMediaInfoTest.png(735, 561)

        val GIF: ByteArray = ChatMediaInfoTest.gif(32, 16)
    }
}
