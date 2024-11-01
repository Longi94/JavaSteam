package `in`.dragonbra.javasteam.util

import `in`.dragonbra.javasteam.util.crypto.CryptoHelper
import `in`.dragonbra.javasteam.util.stream.BinaryReader
import `in`.dragonbra.javasteam.util.stream.BinaryWriter
import `in`.dragonbra.javasteam.util.stream.MemoryStream
import `in`.dragonbra.javasteam.util.stream.SeekOrigin
import org.tukaani.xz.LZMA2Options
import org.tukaani.xz.LZMAInputStream
import org.tukaani.xz.LZMAOutputStream
import java.io.ByteArrayOutputStream
import java.util.zip.DataFormatException

object VZipUtil {
    private const val VZIP_HEADER: Short = 0x5A56 // "VZ" in hex
    private const val VZIP_FOOTER: Short = 0x767A // "vz" in hex
    private const val HEADER_LENGTH = 7 // magic + version + timestamp/crc
    private const val FOOTER_LENGTH = 10 // crc + decompressed size + magic
    private const val VERSION = 'a'

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

            val bytesRead = LZMAInputStream(ms).use { lzmaInput ->
                lzmaInput.read(destination)
            }

            if (verifyChecksum && Utils.crc32(destination).toInt() != outputCrc) {
                throw DataFormatException("CRC does not match decompressed data. VZip data may be corrupted.")
            }

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
