package `in`.dragonbra.javasteam.steam.cdn

import `in`.dragonbra.javasteam.types.ChunkData
import `in`.dragonbra.javasteam.util.Utils
import `in`.dragonbra.javasteam.util.VZipUtil
import `in`.dragonbra.javasteam.util.ZipUtil
import `in`.dragonbra.javasteam.util.crypto.CryptoHelper
import `in`.dragonbra.javasteam.util.stream.MemoryStream
import java.io.IOException
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * Provides a helper function to decrypt and decompress a single depot chunk.
 */
object DepotChunk {

    /**
     * Processes the specified depot key by decrypting the data with the given depot encryption key, and then by decompressing the data.
     * If the chunk has already been processed, this function does nothing.
     * @param info The depot chunk data representing.
     * @param data The encrypted chunk data.
     * @param destination The buffer to receive the decrypted chunk data.
     * @param depotKey The depot decryption key.
     * @exception IOException Thrown if the processed data does not match the expected checksum given in its chunk information.
     * @exception IllegalArgumentException Thrown if the destination size is too small or the depot key is not 32 bytes long
     */
    fun process(
        info: ChunkData,
        data: ByteArray,
        destination: ByteArray,
        depotKey: ByteArray
    ): Int {
        require(destination.size >= info.uncompressedLength) {
            "The destination buffer must be longer than the chunk ${ChunkData::uncompressedLength.name}."
        }
        require(depotKey.size == 32) { "Tried to decrypt depot chunk with non 32 byte key!" }

        // first 16 bytes of input is the ECB encrypted IV
        val keySpec = SecretKeySpec(depotKey, "AES")
        val ecbCipher = Cipher.getInstance("AES/ECB/NoPadding", CryptoHelper.SEC_PROV)
        ecbCipher.init(Cipher.DECRYPT_MODE, keySpec)
        val iv = ByteArray(16)
        val ivBytesRead = ecbCipher.doFinal(data, 0, iv.size, iv)
        require(iv.size == ivBytesRead) { "Failed to decrypt depot chunk iv (${iv.size} != $ivBytesRead)" }

        // With CBC and padding, the decrypted size will always be smaller
        val buffer = ByteArray(data.size - iv.size)
        val cbcCipher = Cipher.getInstance("AES/CBC/PKCS7Padding", CryptoHelper.SEC_PROV)
        cbcCipher.init(Cipher.DECRYPT_MODE, keySpec, IvParameterSpec(iv))

        val writtenDecompressed: Int

        try {
            val bytesWrittenToBuffer = cbcCipher.doFinal(data, iv.size, data.size - iv.size, buffer)

            writtenDecompressed = if (buffer.size > 1 && buffer[0] == 'V'.code.toByte() && buffer[1] == 'Z'.code.toByte()) {
                MemoryStream(buffer, 0, bytesWrittenToBuffer).use { ms ->
                    VZipUtil.decompress(ms, destination, verifyChecksum = false)
                }
            } else {
                MemoryStream(buffer, 0, bytesWrittenToBuffer).use { ms ->
                    ZipUtil.decompress(ms, destination, verifyChecksum = false)
                }
            }
        } catch (e: Exception) {
            throw IOException("Failed to decompress chunk ${Utils.encodeHexString(info.chunkID)}: $e\n${e.stackTraceToString()}")
        }

        if (info.uncompressedLength != writtenDecompressed) {
            throw IOException("Processed data checksum failed to decompress to the expected chunk uncompressed length. (was $writtenDecompressed, should be ${info.uncompressedLength})")
        }

        val dataCrc = Utils.adlerHash(destination.sliceArray(0 until writtenDecompressed))

        if(dataCrc != info.checksum) {
            throw IOException("Processed data checksum is incorrect ($dataCrc != ${info.checksum})! Downloaded depot chunk is corrupt or invalid/wrong depot key?")
        }

        return writtenDecompressed
    }
}
