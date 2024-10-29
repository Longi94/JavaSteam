package `in`.dragonbra.javasteam.util

import `in`.dragonbra.javasteam.util.lzma.CoderPropID
import `in`.dragonbra.javasteam.util.lzma.compress.lzma.Decoder
import `in`.dragonbra.javasteam.util.lzma.compress.lzma.Encoder
import java.io.*

class VZipUtil {
    companion object {
        const val VZIP_HEADER: Short = 0x5A56
        const val VZIP_FOOTER: Short = 0x767A
        const val HEADER_LENGTH: Int = 7 // magic + version + timestamp/crc
        const val FOOTER_LENGTH: Int = 10 // crc + decompressed size + magic

        const val VERSION: Char = 'a'

        fun decompress(inputStream: InputStream, destination: ByteArray, verifyChecksum: Boolean = true): Int {
            val reader = DataInputStream(inputStream)
            if (reader.readShort().toInt() != VZIP_HEADER.toInt()) {
                throw Exception("Expecting VZipHeader at start of stream")
            }

            if (reader.readByte().toInt().toChar() != VERSION) {
                throw Exception("Expecting VZip version 'a'")
            }

            // Sometimes this is a creation timestamp (e.g. for Steam Client VZips).
            // Sometimes this is a CRC32 (e.g. for depot chunks).
            reader.readInt() // creationTimestampOrSecondaryCRC

            // this is 5 bytes of LZMA properties
            val propertyBits = reader.readByte()
            val dictionarySize = reader.readInt()
            val compressedBytesOffset = inputStream.available()

            // jump to the end of the buffer to read the footer
            inputStream.skip(inputStream.available() - FOOTER_LENGTH.toLong())
            val sizeCompressed = inputStream.available() - compressedBytesOffset - FOOTER_LENGTH
            val outputCRC = reader.readInt()
            val sizeDecompressed = reader.readInt()

            if (reader.readShort().toInt() != VZIP_FOOTER.toInt()) {
                throw Exception("Expecting VZipFooter at end of stream")
            }

            if (destination.size < sizeDecompressed) {
                throw IllegalArgumentException("The destination buffer is smaller than the decompressed data size.")
            }

            // jump back to the beginning of the compressed data
            inputStream.reset()
            inputStream.skip(compressedBytesOffset.toLong())

            val decoder = Decoder()

            // If the value of dictionary size in properties is smaller than (1 << 12),
            // the LZMA decoder must set the dictionary size variable to (1 << 12).
            val windowBuffer = ByteArray(maxOf(1 shl 12, dictionarySize))

            try {
                decoder.steamKitSetDecoderProperties(propertyBits, dictionarySize, windowBuffer)

                val outStream = ByteArrayOutputStream()
                decoder.code(inputStream, outStream, sizeCompressed.toLong(), sizeDecompressed.toLong(), null)
                ByteArrayInputStream(outStream.toByteArray()).read(destination)
            } finally {
                // No need for explicit buffer return in Kotlin/JVM
            }

            if (verifyChecksum && Utils.crc32(destination.sliceArray(0 until sizeDecompressed)).toInt() != outputCRC) {
                throw IOException("CRC does not match decompressed data. VZip data may be corrupted.")
            }

            return sizeDecompressed
        }

        fun compress(buffer: ByteArray): ByteArray {
            val ms = ByteArrayOutputStream()
            val writer = DataOutputStream(ms)
            val crc = Utils.crc32(buffer).toInt()

            writer.writeShort(VZIP_HEADER.toInt())
            writer.writeByte(VERSION.code)
            writer.writeInt(crc)

            val dictionary = 1 shl 23
            val posStateBits = 2
            val litContextBits = 3
            val litPosBits = 0
            val algorithm = 2
            val numFastBytes = 128

            val propIDs = arrayOf(
                CoderPropID.DictionarySize,
                CoderPropID.PosStateBits,
                CoderPropID.LitContextBits,
                CoderPropID.LitPosBits,
                CoderPropID.Algorithm,
                CoderPropID.NumFastBytes,
                CoderPropID.MatchFinder,
                CoderPropID.EndMarker
            )

            val properties = arrayOf(
                dictionary,
                posStateBits,
                litContextBits,
                litPosBits,
                algorithm,
                numFastBytes,
                "bt4",
                false
            )

            val encoder = Encoder()
            encoder.setCoderProperties(propIDs, arrayOf(properties))
            encoder.writeCoderProperties(ms)

            ByteArrayInputStream(buffer).use { input ->
                encoder.code(input, ms, -1, -1, null)
            }

            writer.writeInt(crc)
            writer.writeInt(buffer.size)
            writer.writeShort(VZIP_FOOTER.toInt())

            return ms.toByteArray()
        }
    }
}
