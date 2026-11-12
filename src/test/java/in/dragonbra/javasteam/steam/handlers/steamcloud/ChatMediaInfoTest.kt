package `in`.dragonbra.javasteam.steam.handlers.steamcloud

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream

class ChatMediaInfoTest {

    @Test
    fun readsPng() {
        Assertions.assertEquals(ChatMediaInfo("image/png", 640, 480), ChatMediaInfo.detect(png(640, 480)))
    }

    @Test
    fun readsGif() {
        Assertions.assertEquals(ChatMediaInfo("image/gif", 500, 281), ChatMediaInfo.detect(gif(500, 281)))
        Assertions.assertEquals("image/gif", ChatMediaInfo.detect(gif(1, 1, version = "GIF87a")).mimeType)
    }

    @Test
    fun readsJpeg() {
        Assertions.assertEquals(ChatMediaInfo("image/jpeg", 1920, 1080), ChatMediaInfo.detect(jpeg(1920, 1080)))
    }

    @Test
    fun readsProgressiveJpegBehindOtherSegments() {
        // The frame header is not the first segment, and C4 before it is a huffman table and not a frame
        val data = jpeg(800, 600, frameMarker = 0xC2, segmentsBefore = listOf(0xE1 to 40, 0xDB to 67, 0xC4 to 30))

        Assertions.assertEquals(ChatMediaInfo("image/jpeg", 800, 600), ChatMediaInfo.detect(data))
    }

    @Test
    fun readsVideosWithoutASize() {
        val webm = byteArrayOf(0x1A, 0x45, 0xDF.toByte(), 0xA3.toByte(), 0x01, 0x00, 0x00, 0x00)
        val mpegProgramStream = byteArrayOf(0x00, 0x00, 0x01, 0xBA.toByte(), 0x44, 0x00, 0x04, 0x00)
        val mpegVideoStream = byteArrayOf(0x00, 0x00, 0x01, 0xB3.toByte(), 0x14, 0x00, 0xF0.toByte(), 0x13)
        val ogv = "OggS".toByteArray() + ByteArray(23)

        Assertions.assertEquals(ChatMediaInfo("video/webm", 0, 0), ChatMediaInfo.detect(webm))
        Assertions.assertEquals(ChatMediaInfo("video/mpeg", 0, 0), ChatMediaInfo.detect(mpegProgramStream))
        Assertions.assertEquals(ChatMediaInfo("video/mpeg", 0, 0), ChatMediaInfo.detect(mpegVideoStream))
        Assertions.assertEquals(ChatMediaInfo("video/ogv", 0, 0), ChatMediaInfo.detect(ogv))
    }

    @Test
    fun namesTheExtensionAfterTheType() {
        Assertions.assertEquals("png", ChatMediaInfo.detect(png(1, 1)).extension)
        Assertions.assertEquals("jpeg", ChatMediaInfo.detect(jpeg(1, 1)).extension)
        Assertions.assertEquals("ogv", ChatMediaInfo.detect("OggS".toByteArray()).extension)
    }

    @Test
    fun rejectsOtherFormats() {
        val mp4 = byteArrayOf(0x00, 0x00, 0x00, 0x18) + "ftypmp42".toByteArray()

        Assertions.assertThrows(IllegalArgumentException::class.java) { ChatMediaInfo.detect(ByteArray(0)) }
        Assertions.assertThrows(IllegalArgumentException::class.java) { ChatMediaInfo.detect("RIFF....WEBPVP8 ".toByteArray()) }
        Assertions.assertThrows(IllegalArgumentException::class.java) { ChatMediaInfo.detect("BM plus some bitmap".toByteArray()) }
        Assertions.assertThrows(IllegalArgumentException::class.java) { ChatMediaInfo.detect(mp4) }
    }

    @Test
    fun rejectsHeadersCutShort() {
        Assertions.assertThrows(IllegalArgumentException::class.java) { ChatMediaInfo.detect(png(640, 480).copyOf(20)) }
        Assertions.assertThrows(IllegalArgumentException::class.java) { ChatMediaInfo.detect(gif(32, 16).copyOf(8)) }
        Assertions.assertThrows(IllegalArgumentException::class.java) {
            ChatMediaInfo.detect(
                jpeg(
                    640,
                    480
                ).copyOf(24)
            )
        }

        // A JPEG that never gets to a frame header
        Assertions.assertThrows(IllegalArgumentException::class.java) {
            ChatMediaInfo.detect(byteArrayOf(0xFF.toByte(), 0xD8.toByte(), 0xFF.toByte(), 0xD9.toByte()))
        }
    }

    companion object {
        /**
         * The signature and IHDR chunk of a PNG.
         */
        fun png(width: Int, height: Int): ByteArray = build {
            write(byteArrayOf(0x89.toByte(), 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A))
            writeInt(13)
            writeBytes("IHDR")
            writeInt(width)
            writeInt(height)
            write(byteArrayOf(8, 6, 0, 0, 0))
        }

        /**
         * The header and logical screen descriptor of a GIF.
         */
        fun gif(width: Int, height: Int, version: String = "GIF89a"): ByteArray = build {
            writeBytes(version)
            writeShort(Integer.reverseBytes(width) ushr 16)
            writeShort(Integer.reverseBytes(height) ushr 16)
            write(byteArrayOf(0, 0, 0))
        }

        /**
         * A JPEG up to and including its frame header.
         *
         * @param segmentsBefore Marker and payload size of segments to put ahead of the frame header.
         */
        fun jpeg(
            width: Int,
            height: Int,
            frameMarker: Int = 0xC0,
            segmentsBefore: List<Pair<Int, Int>> = listOf(0xE0 to 14),
        ): ByteArray = build {
            writeShort(0xFFD8)

            segmentsBefore.forEach { (marker, payloadSize) ->
                writeShort(0xFF00 or marker)
                writeShort(payloadSize + 2)
                write(ByteArray(payloadSize))
            }

            writeShort(0xFF00 or frameMarker)
            writeShort(17)
            writeByte(8)
            writeShort(height)
            writeShort(width)
            write(ByteArray(10))
        }

        private fun build(block: DataOutputStream.() -> Unit): ByteArray {
            val bytes = ByteArrayOutputStream()
            DataOutputStream(bytes).use(block)
            return bytes.toByteArray()
        }
    }
}
