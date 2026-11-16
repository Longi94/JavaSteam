package `in`.dragonbra.javasteam.depotdownloader

import kotlinx.coroutines.runBlocking
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import okhttp3.OkHttpClient
import okio.Buffer
import okio.FileSystem
import okio.Path
import okio.Path.Companion.toOkioPath
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.IOException
import java.util.Random

// Kotlin, not Java: HttpClient.downloadFile is a suspend function.
class HttpClientDownloadFileTest {

    private lateinit var server: MockWebServer

    private lateinit var destination: Path

    private val filesystem = FileSystem.SYSTEM

    @BeforeEach
    fun setUp(@TempDir tempDir: java.nio.file.Path) {
        server = MockWebServer()
        server.start()

        destination = tempDir.toOkioPath() / "workshop_file.bin"
    }

    @AfterEach
    fun tearDown() {
        server.close()
    }

    @Test
    fun downloadsTheFile() {
        // Larger than one okio segment and one socket read.
        val content = ByteArray(3 * 1024 * 1024 + 17).also { Random(7).nextBytes(it) }
        server.enqueue(MockResponse.Builder().code(200).body(Buffer().write(content)).build())

        val written = download()

        assertEquals(content.size.toLong(), written)
        assertArrayEquals(content, filesystem.read(destination) { readByteArray() })

        val request = server.takeRequest()
        assertEquals("GET", request.method)
        assertEquals("/ugc/123/file.bin", request.url.encodedPath)
        assertTrue(request.headers["User-Agent"]!!.startsWith("JavaSteam-DepotDownloader/"))
    }

    @Test
    fun replacesAnExistingFile() {
        filesystem.write(destination) { writeUtf8("a much longer leftover from an earlier attempt") }
        server.enqueue(MockResponse.Builder().code(200).body("new").build())

        assertEquals(3L, download())
        assertEquals("new", filesystem.read(destination) { readUtf8() })
    }

    @Test
    fun downloadsAnEmptyFile() {
        server.enqueue(MockResponse.Builder().code(200).build())

        assertEquals(0L, download())
        assertEquals(0L, filesystem.metadata(destination).size)
    }

    @Test
    fun failsOnAnErrorStatusWithoutWritingTheErrorPage() {
        server.enqueue(MockResponse.Builder().code(404).body("<html>Not Found</html>").build())

        val exception = assertThrows(DepotDownloaderException::class.java) { download() }

        assertTrue(exception.message!!.contains("404"))
        assertFalse(filesystem.exists(destination))
    }

    @Test
    fun failsWhenTheServerIsUnreachable() {
        val url = server.url("/ugc/123/file.bin").toString()
        server.close()

        assertThrows(IOException::class.java) {
            runBlocking { HttpClient.downloadFile(OkHttpClient(), url, destination, filesystem) }
        }
        assertFalse(filesystem.exists(destination))
    }

    private fun download(): Long = runBlocking {
        HttpClient.downloadFile(OkHttpClient(), server.url("/ugc/123/file.bin").toString(), destination, filesystem)
    }
}
