package `in`.dragonbra.javasteam.steam.cdn

import `in`.dragonbra.javasteam.types.ChunkData
import `in`.dragonbra.javasteam.util.Utils
import `in`.dragonbra.javasteam.util.VZipUtil
import `in`.dragonbra.javasteam.util.ZipUtil
import `in`.dragonbra.javasteam.util.crypto.CryptoHelper
import `in`.dragonbra.javasteam.util.log.LogManager
import `in`.dragonbra.javasteam.util.log.Logger
import `in`.dragonbra.javasteam.util.stream.MemoryStream
import java.io.ByteArrayInputStream
import java.io.IOException
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * Provides a helper function to decrypt and decompress a single depot chunk.
 */
object DepotChunk {
    private val logger: Logger = LogManager.getLogger(DepotChunk::class.java)

    /**
     * Processes the specified depot key by decrypting the data with the given depot encryption key, and then by decompressing the data.
     * If the chunk has already been processed, this function does nothing.
     * @param info The depot chunk data representing.
     * @param data The encrypted chunk data.
     * @param destination The buffer to receive the decrypted chunk data.
     * @param depotKey The depot decryption key.
     * @exception IllegalArgumentException Thrown if there are issues with the given arguments
     * @exception IOException Thrown if the processed data does not match the expected checksum given in it's chunk information.
     */
    fun process(
        info: ChunkData,
        data: ByteArray,
        destination: ByteArray,
        depotKey: ByteArray
    ): Int {
        require(info != null) { "info cannot be null" }
        require(depotKey != null) { "depotKey cannot be null" }

        require(destination.size >= info.uncompressedLength) {
            "The destination buffer must be longer than the chunk ${ChunkData::uncompressedLength.name}."
        }

        assert(depotKey.size == 32) { "Tried to decrypt depot chunk with non 32 byte key!" }

        val aes = Cipher.getInstance("AES/CBC/PKCS7Padding", CryptoHelper.SEC_PROV)
        val keySpec = SecretKeySpec(depotKey, "AES")

        // first 16 bytes of input is the ECB encrypted IV
        val ecbCipher = Cipher.getInstance("AES/ECB/NoPadding", CryptoHelper.SEC_PROV)
        ecbCipher.init(Cipher.DECRYPT_MODE, keySpec)
        val iv = ecbCipher.doFinal(data, 0, 16)

        // With CBC and padding, the decrypted size will always be smaller
//            val buffer = ByteArray(data.size - iv.size)

        var writtenDecompressed = 0

        try {
            aes.init(Cipher.DECRYPT_MODE, keySpec, IvParameterSpec(iv))
            val decrypted = aes.doFinal(data, iv.size, data.size - iv.size)

            writtenDecompressed = if (decrypted.size > 1 && decrypted[0] == 'V'.toByte() && decrypted[1] == 'Z'.toByte()) {
                VZipUtil.decompress(MemoryStream(decrypted), destination, verifyChecksum = false)
            } else {
                ZipUtil.decompress(MemoryStream(decrypted), destination, verifyChecksum = false)
            }
        } catch (e: Exception) {
            logger.error("Failed to decompress chunk ${Utils.encodeHexString(info.chunkID)}: $e")
        } finally {
                // No need for explicit buffer return in Kotlin/JVM
        }

        if (info.uncompressedLength != writtenDecompressed) {
            throw IOException("Processed data checksum failed to decompress to the expected chunk uncompressed length. (was $writtenDecompressed, should be ${info.uncompressedLength})")
        }

        val dataCrc = Utils.adlerHash(destination.sliceArray(0 until writtenDecompressed))

        if(dataCrc != info.checksum) {
            throw IOException("Processed data checksum is incorrect! Downloaded depot chunk is corrupt or invalid/wrong depot key?")
        }

        return writtenDecompressed
    }
}
