package `in`.dragonbra.javasteam.util

import `in`.dragonbra.javasteam.util.compat.readNBytesCompat
import `in`.dragonbra.javasteam.util.log.LogManager
import `in`.dragonbra.javasteam.util.stream.BinaryReader
import `in`.dragonbra.javasteam.util.stream.MemoryStream
import `in`.dragonbra.javasteam.util.stream.SeekOrigin
import org.tukaani.xz.LZMAInputStream
import java.util.zip.*
import kotlin.math.max

@Suppress("SpellCheckingInspection", "unused")
object VZipUtil {

    private val logger = LogManager.getLogger<VZipUtil>()

    private const val VZIP_HEADER: Short = 0x5A56 // "VZ" in hex
    private const val VZIP_FOOTER: Short = 0x767A // "vz" in hex
    private const val HEADER_LENGTH = 7 // magic + version + timestamp/crc
    private const val FOOTER_LENGTH = 10 // crc + decompressed size + magic

    private const val VERSION: Byte = 'a'.code.toByte()

    // Thread-local window buffer pool to avoid repeated allocations
    private val windowBufferPool = ThreadLocal.withInitial {
        ByteArray(1 shl 23) // 8MB max size
    }

    @JvmStatic
    fun decompress(ms: MemoryStream, destination: ByteArray, verifyChecksum: Boolean = true): Int {
        try {
            BinaryReader(ms).use { reader ->
                if (reader.readShort() != VZIP_HEADER) {
                    throw IllegalArgumentException("Expecting VZipHeader at start of stream")
                }

                if (reader.readByte() != VERSION) {
                    throw IllegalArgumentException("Expecting VZip version 'a'")
                }

                // Sometimes this is a creation timestamp (e.g. for Steam Client VZips).
                // Sometimes this is a CRC32 (e.g. for depot chunks).
                /* val creationTimestampOrSecondaryCRC: UInt = */
                reader.readInt()

                // this is 5 bytes of LZMA properties
                val propertyBits = reader.readByte()
                val dictionarySize = reader.readInt()
                val compressedBytesOffset = ms.position

                // jump to the end of the buffer to read the footer
                ms.seek((-FOOTER_LENGTH).toLong(), SeekOrigin.END)

                val outputCrc = reader.readInt()
                val sizeDecompressed = reader.readInt()

                if (reader.readShort() != VZIP_FOOTER) {
                    throw IllegalArgumentException("Expecting VZipFooter at end of stream")
                }

                if (destination.size < sizeDecompressed) {
                    throw IllegalArgumentException("The destination buffer is smaller than the decompressed data size.")
                }

                // jump back to the beginning of the compressed data
                ms.position = compressedBytesOffset

                // If the value of dictionary size in properties is smaller than (1 << 12),
                // the LZMA decoder must set the dictionary size variable to (1 << 12).
                val windowSize = max(1 shl 12, dictionarySize)
                val windowBuffer = if (windowSize <= (1 shl 23)) {
                    windowBufferPool.get() // Reuse thread-local buffer
                } else {
                    ByteArray(windowSize) // Fallback for unusually large windows
                }
                val bytesRead = LZMAInputStream(
                    ms,
                    sizeDecompressed.toLong(),
                    propertyBits,
                    dictionarySize,
                    windowBuffer
                ).use { lzmaInput ->
                    lzmaInput.readNBytesCompat(destination, 0, sizeDecompressed)
                }

                if (verifyChecksum) {
                    val actualCrc = Utils.crc32(destination, 0, bytesRead).toInt()
                    if (actualCrc != outputCrc) {
                        throw DataFormatException("CRC does not match decompressed data. VZip data may be corrupted.")
                    }
                }

                return bytesRead
            }
        } catch (e: NoClassDefFoundError) {
            logger.error("Missing implementation of org.tukaani:xz")
            throw e
        } catch (e: ClassNotFoundException) {
            logger.error("Missing implementation of org.tukaani:xz")
            throw e
        }
    }

    @JvmStatic
    fun compress(buffer: ByteArray): ByteArray {
        throw Exception("VZipUtil.compress is not implemented.")
        // try {
        // } catch (e: NoClassDefFoundError) {
        //     logger.error("Missing implementation of org.tukaani:xz")
        //     throw e
        // } catch (e: ClassNotFoundException) {
        //     logger.error("Missing implementation of org.tukaani:xz")
        //     throw e
        // }
    }
}
