package `in`.dragonbra.javasteam.util

import `in`.dragonbra.javasteam.util.crypto.CryptoHelper
import `in`.dragonbra.javasteam.util.log.LogManager
import `in`.dragonbra.javasteam.util.log.Logger
import `in`.dragonbra.javasteam.util.stream.BinaryReader
import `in`.dragonbra.javasteam.util.stream.BinaryWriter
import `in`.dragonbra.javasteam.util.stream.MemoryStream
import `in`.dragonbra.javasteam.util.stream.SeekOrigin
import org.tukaani.xz.LZMA2Options
import org.tukaani.xz.LZMAInputStream
import org.tukaani.xz.LZMAOutputStream
import java.io.ByteArrayOutputStream
import java.util.zip.*
import kotlin.math.max


object VZipUtil {
    private const val VZIP_HEADER: Short = 0x5A56 // "VZ" in hex
    private const val VZIP_FOOTER: Short = 0x767A // "vz" in hex
    private const val HEADER_LENGTH = 7 // magic + version + timestamp/crc
    private const val FOOTER_LENGTH = 10 // crc + decompressed size + magic
    private const val VERSION = 'a'
    private const val kNumPosStatesBitsMax = 4
    private val logger: Logger = LogManager.getLogger(VZipUtil::class.java)

    fun decompress(ms: MemoryStream, destination: ByteArray, verifyChecksum: Boolean = true): Int {
        BinaryReader(ms).use { reader ->
            if (reader.readShort() != VZIP_HEADER) {
                throw IllegalArgumentException("Expecting VZipHeader at start of stream")
            }
            if (reader.readChar() != VERSION) {
                throw IllegalArgumentException("Expecting VZip version 'a'")
            }

            // Sometimes this is a creation timestamp (e.g. for Steam Client VZips).
            // Sometimes this is a CRC32 (e.g. for depot chunks).
            reader.readInt()

            // this is 5 bytes of LZMA properties
            val propertyBits = reader.readByte()
            val dictionarySize = reader.readInt()
            val compressedBytesOffset = ms.position

//            val lc: Int = propertyBits % 9
//            val remainder: Int = propertyBits / 9
//            val lp = remainder % 5
//            val pb = remainder / 5
//            if (pb > kNumPosStatesBitsMax || dictionarySize < (1 shl 12)) throw InvalidParamException()

            // jump to the end of the buffer to read the footer

            // Calculate compressed data boundaries
            ms.seek((-FOOTER_LENGTH).toLong(), SeekOrigin.END)
            var sizeCompressed = ms.position - compressedBytesOffset
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

            val windowBuffer = ByteArray(max(1 shl 12, dictionarySize))
            val bytesRead = LZMAInputStream(ms, sizeDecompressed.toLong(), propertyBits, dictionarySize, windowBuffer).use { lzmaInput ->
                lzmaInput.readNBytes(destination, 0, sizeDecompressed)
//                val output = lzmaInput.readAllBytes()
//                System.arraycopy(output, 0, destination, 0, destination.size)
//                output.size
            }
//            val bytesRead = LZMAInputStream(ms, sizeDecompressed.toLong(), lc, lp, pb, dictionarySize, windowBuffer).use { lzmaInput ->
//                lzmaInput.readNBytes(destination, 0, sizeDecompressed)
//            }

            if (verifyChecksum && Utils.crc32(destination).toInt() != outputCrc) {
                throw DataFormatException("CRC does not match decompressed data. VZip data may be corrupted.")
            }

//            val startPos = ms.position
//            if (reader.readShort() != VZIP_HEADER) {
//                throw IllegalArgumentException("Expecting VZipHeader at start of stream")
//            }
//            if (reader.readChar() != VERSION) {
//                throw IllegalArgumentException("Expecting VZip version 'a'")
//            }
//
//            // Sometimes this is a creation timestamp (e.g. for Steam Client VZips).
//            // Sometimes this is a CRC32 (e.g. for depot chunks).
//            reader.readInt()
//
//            // this is 5 bytes of LZMA properties
//            val propertyBits = reader.readByte()
//            val dictionarySize = reader.readInt()
//            val compressedBytesOffset = ms.position
//
//            // jump to the end of the buffer to read the footer
//
//            // Calculate compressed data boundaries
//            ms.seek((-FOOTER_LENGTH).toLong(), SeekOrigin.END)
//            var sizeCompressed = ms.position - compressedBytesOffset
//            val outputCrc = reader.readInt()
//            val sizeDecompressed = reader.readInt()
//
//            ms.position = startPos
//            val windowBuffer = ByteArray(max(1 shl 12, dictionarySize))
//            val bytesRead = LZMAInputStream(ms, sizeDecompressed.toLong(), propertyBits, dictionarySize, windowBuffer).use { lzmaInput ->
//                lzmaInput.readNBytes(destination, 0, sizeDecompressed)
//            }

            return bytesRead
        }
    }

    fun compress(buffer: ByteArray): ByteArray {
        ByteArrayOutputStream().use { ms ->
            BinaryWriter(ms).use { writer ->
                val crc = CryptoHelper.crcHash(buffer)
                writer.writeShort(VZIP_HEADER)
                writer.writeChar(VERSION)
                writer.write(crc)

                // Configure LZMA options to match SteamKit2's settings
                val options = LZMA2Options().apply {
                    dictSize = 1 shl 23  // 8MB dictionary
                    setPreset(2)  // Algorithm setting
                    niceLen = 128  // numFastBytes equivalent
                    matchFinder = LZMA2Options.MF_BT4
                    mode = LZMA2Options.MODE_NORMAL
                }

                // Write LZMA-compressed data
                LZMAOutputStream(ms, options, false).use { lzmaStream ->
                    lzmaStream.write(buffer)
                }

                writer.write(crc)
                writer.writeInt(buffer.size)
                writer.writeShort(VZIP_FOOTER)

                return ms.toByteArray()
            }
        }
    }
}
