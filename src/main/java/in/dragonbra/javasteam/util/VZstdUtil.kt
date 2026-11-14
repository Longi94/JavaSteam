package `in`.dragonbra.javasteam.util

import com.github.luben.zstd.RecyclingBufferPool
import com.github.luben.zstd.ZstdInputStream
import `in`.dragonbra.javasteam.util.log.LogManager
import java.io.ByteArrayInputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder

object VZstdUtil {

    private const val VZSTD_HEADER: Int = 0x615A5356
    private const val STREAM_CHUNK_SIZE = 64 * 1024 // 64KB chunks
    private const val HEADER_SIZE = 8
    private const val FOOTER_SIZE = 15

    private val logger = LogManager.getLogger<VZstdUtil>()

    /**
     * Decompresses VZstd data.
     * @param buffer The compressed data, starting at index 0.
     * @param destination The buffer to receive the decompressed data.
     * @param verifyChecksum Whether to verify the CRC32 of the decompressed data.
     * @param length Number of bytes of [buffer] that hold the compressed data. Defaults to all of it.
     * @return The number of bytes written to [destination].
     */
    @Throws(IOException::class, IllegalArgumentException::class)
    @JvmStatic
    @JvmOverloads
    fun decompress(
        buffer: ByteArray,
        destination: ByteArray,
        verifyChecksum: Boolean = false,
        length: Int = buffer.size,
    ): Int {
        require(length in 0..buffer.size) { "length ($length) is outside the buffer (size ${buffer.size})." }

        if (length < HEADER_SIZE + FOOTER_SIZE) {
            throw IOException("Buffer too small to contain VZstd header and footer")
        }

        val byteBuffer = ByteBuffer.wrap(buffer).order(ByteOrder.LITTLE_ENDIAN) // Convert the buffer.

        val header = byteBuffer.getInt(0)
        if (header != VZSTD_HEADER) {
            throw IOException("Expecting VZstdHeader at start of stream")
        }

        // val crc32 = byteBuffer.getInt(4)

        // Read footer
        val footerOffset = length - FOOTER_SIZE
        val crc32Footer = byteBuffer.getInt(footerOffset)
        val sizeDecompressed = byteBuffer.getInt(footerOffset + 4)

        // This part gets spammed a lot, so we'll mute this.
        // if (crc32 == crc32Footer) {
        //     // They write CRC32 twice?
        //     logger.debug("CRC32 appears to be written twice in the data")
        // }

        if (buffer[length - 3] != 'z'.code.toByte() ||
            buffer[length - 2] != 's'.code.toByte() ||
            buffer[length - 1] != 'v'.code.toByte()
        ) {
            throw IOException("Expecting VZstdFooter at end of stream")
        }

        if (destination.size < sizeDecompressed) {
            throw IllegalArgumentException("The destination buffer is smaller than the decompressed data size.")
        }

        try {
            // Use streaming decompression to avoid long JNI critical locks
            var totalDecompressed = 0

            // Use direct ByteArrayInputStream with offset to avoid copying compressed data
            // The third argument is a length from the offset, not an end position.
            ByteArrayInputStream(buffer, HEADER_SIZE, length - HEADER_SIZE - FOOTER_SIZE).use { byteStream ->
                // The pool hands the ~128KB source buffer back on close(), so chunks reuse it instead of
                // allocating one each. It is thread-safe. Keep this reference inside the try block:
                // zstd is 'compileOnly', and the pool class must not load before the catch below can see it.
                ZstdInputStream(byteStream, RecyclingBufferPool.INSTANCE).use { zstdStream ->
                    var bytesRead: Int
                    var offset = 0

                    // Read in chunks to break up JNI locks
                    while (offset < sizeDecompressed) {
                        val toRead = minOf(STREAM_CHUNK_SIZE, sizeDecompressed - offset)
                        bytesRead = zstdStream.read(destination, offset, toRead)

                        if (bytesRead == -1) break

                        offset += bytesRead
                        totalDecompressed += bytesRead
                    }
                }
            }

            if (totalDecompressed != sizeDecompressed) {
                throw IOException("Failed to decompress Zstd (expected $sizeDecompressed bytes, got $totalDecompressed).")
            }

            if (verifyChecksum) {
                val calculatedCrc = Utils.crc32(destination, 0, sizeDecompressed).toInt()
                if (calculatedCrc != crc32Footer) {
                    throw IOException("CRC does not match decompressed data. VZstd data may be corrupted.")
                }
            }

            return sizeDecompressed
        } catch (e: NoClassDefFoundError) {
            // Zstd is a 'compileOnly' dependency. If it's missing, throw the correct type of error.
            logger.error("Missing implementation of com.github.luben:zstd-jni")
            throw e
        } catch (e: Exception) {
            // Catch all for the Zstd library.
            throw IOException("Failed to decompress Zstd data: ${e.message}", e)
        }
    }
}
