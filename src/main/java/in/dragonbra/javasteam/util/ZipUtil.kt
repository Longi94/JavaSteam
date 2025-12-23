package `in`.dragonbra.javasteam.util

import `in`.dragonbra.javasteam.util.compat.readNBytesCompat
import `in`.dragonbra.javasteam.util.stream.MemoryStream
import java.util.zip.ZipInputStream

object ZipUtil {

    @JvmStatic
    fun decompress(ms: MemoryStream, destination: ByteArray, verifyChecksum: Boolean = true): Int {
        ZipInputStream(ms, Charsets.UTF_8).use { zip ->
            val entry = zip.nextEntry
                ?: throw IllegalArgumentException("Did not find any zip entries in the given stream")

            val sizeDecompressed = entry.size.toInt()

            if (destination.size < sizeDecompressed) {
                throw IllegalArgumentException("The destination buffer is smaller than the decompressed data size.")
            }

            val bytesRead = zip.readNBytesCompat(destination, 0, sizeDecompressed)

            if (zip.nextEntry != null) {
                throw IllegalArgumentException("Given stream should only contain one zip entry")
            }

            if (verifyChecksum && Utils.crc32(destination, 0, bytesRead) != entry.crc) {
                throw Exception("Checksum validation failed for decompressed file")
            }

            return bytesRead
        }
    }
}
