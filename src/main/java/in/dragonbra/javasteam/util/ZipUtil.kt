package `in`.dragonbra.javasteam.util

import `in`.dragonbra.javasteam.util.log.LogManager
import `in`.dragonbra.javasteam.util.log.Logger
import `in`.dragonbra.javasteam.util.stream.MemoryStream
import java.util.zip.ZipInputStream

object ZipUtil {
    private val logger: Logger = LogManager.getLogger(ZipUtil::class.java)

    fun decompress(ms: MemoryStream, destination: ByteArray, verifyChecksum: Boolean = true): Int {
        ZipInputStream(ms, Charsets.UTF_8).use { zip ->
            val entry = zip.nextEntry ?: throw IllegalArgumentException("Did not find any zip entries in the given stream")
            logger.debug("Found first zip entry of stream")

            val sizeDecompressed = entry.size.toInt()
            logger.debug("Size of zip entry is $sizeDecompressed")

            if (destination.size < sizeDecompressed) {
                throw IllegalArgumentException("The destination buffer is smaller than the decompressed data size.")
            }
            logger.debug("Size of destination (${destination.size}) is adequate for zip entry")

            val bytesRead = zip.readNBytes(destination, 0, sizeDecompressed)
            logger.debug("Extracted ($bytesRead) zip entry into destination")

            if (zip.nextEntry != null) {
                throw IllegalArgumentException("Given stream should only contain one zip entry")
            }

            if (verifyChecksum && Utils.crc32(destination.sliceArray(0 until sizeDecompressed)) != entry.crc) {
                throw Exception("Checksum validation failed for decompressed file")
            }

            return bytesRead
        }
    }
}
