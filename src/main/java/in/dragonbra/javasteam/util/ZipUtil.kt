package `in`.dragonbra.javasteam.util

import `in`.dragonbra.javasteam.util.stream.MemoryStream
import java.util.zip.ZipInputStream

object ZipUtil {
    fun decompress(ms: MemoryStream, destination: ByteArray, verifyChecksum: Boolean = true): Int {
        ZipInputStream(ms).use { zip ->
            val entry = zip.nextEntry ?: throw IllegalArgumentException("Expected the zip to contain at least one file")

            val sizeDecompressed = entry.size.toInt()

            if (destination.size < sizeDecompressed) {
                throw IllegalArgumentException("The destination buffer is smaller than the decompressed data size.")
            }

            zip.read(destination, 0, sizeDecompressed)

            if (verifyChecksum && Utils.crc32(destination.sliceArray(0 until sizeDecompressed)) != entry.crc) {
                throw Exception("Checksum validation failed for decompressed file")
            }

            return sizeDecompressed
        }
    }
}
