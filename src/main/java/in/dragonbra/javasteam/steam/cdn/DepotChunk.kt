package `in`.dragonbra.javasteam.steam.cdn

import `in`.dragonbra.javasteam.types.ChunkData
import `in`.dragonbra.javasteam.util.Utils
import `in`.dragonbra.javasteam.util.VZipUtil
import `in`.dragonbra.javasteam.util.ZipUtil
import `in`.dragonbra.javasteam.util.crypto.CryptoHelper
import `in`.dragonbra.javasteam.util.log.LogManager
import `in`.dragonbra.javasteam.util.log.Logger
import `in`.dragonbra.javasteam.util.stream.MemoryStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * Provides a helper function to decrypt and decompress a single depot chunk.
 */
object DepotChunk {
    private val logger: Logger = LogManager.getLogger(DepotChunk::class.java)

//    fun adlerHash(input: ByteArray): UInt {
//        var a = 0u
//        var b = 0u
//        for (i in input.indices) {
//            a = (a + input[i].toUInt()) % 65521u
//            b = (b + a) % 65521u
//        }
//
//        return a or (b shl 16)
//    }

    /**
     * Processes the specified depot key by decrypting the data with the given depot encryption key, and then by decompressing the data.
     * If the chunk has already been processed, this function does nothing.
     * @param info The depot chunk data representing.
     * @param data The encrypted chunk data.
     * @param destination The buffer to receive the decrypted chunk data.
     * @param depotKey The depot decryption key.
     * @exception IOException Thrown if the processed data does not match the expected checksum given in it's chunk information.
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

        val aes = Cipher.getInstance("AES/CBC/PKCS7Padding", CryptoHelper.SEC_PROV)
        val keySpec = SecretKeySpec(depotKey, "AES")

        // first 16 bytes of input is the ECB encrypted IV
        val ecbCipher = Cipher.getInstance("AES/ECB/NoPadding", CryptoHelper.SEC_PROV)
        ecbCipher.init(Cipher.DECRYPT_MODE, keySpec)
        val iv = ByteArray(16)
        val ivBytesRead = ecbCipher.doFinal(data, 0, iv.size, iv)
        require(iv.size == ivBytesRead) { "Failed to decrypt depot chunk iv (${iv.size} != $ivBytesRead)" }

        // With CBC and padding, the decrypted size will always be smaller
//            val buffer = ByteArray(data.size - iv.size)
        aes.init(Cipher.DECRYPT_MODE, keySpec, IvParameterSpec(iv))
//        val outputSize = aes.getOutputSize(data.size - iv.size)
        val buffer = ByteArray(data.size - iv.size)
        val bytesWrittenToBuffer: Int

        val writtenDecompressed: Int

        try {
            logger.debug("Expected aes output size of chunk ${Utils.encodeHexString(info.chunkID)} is ${aes.getOutputSize(data.size - iv.size)} for input size of ${data.size - iv.size}")
//            val cipherData = ByteArray(data.size - iv.size)
//            System.arraycopy(data, iv.size, cipherData, 0, cipherData.size)
//            val written = aes.doFinal(cipherData, 0, cipherData.size, buffer)
            bytesWrittenToBuffer = aes.doFinal(data, iv.size, data.size - iv.size, buffer)
//            bytesWrittenToBuffer = aes.update(data, iv.size, data.size - iv.size, buffer)
            logger.debug("Actually written bytes after doFinal of chunk ${Utils.encodeHexString(info.chunkID)} is $bytesWrittenToBuffer")
//            logger.debug("Decrypting chunk ${Utils.encodeHexString(info.chunkID)} with depot key: [${depotKey.joinToString(", ")}] (${depotKey.size})")
//            val decrypted = CryptoHelper.symmetricDecrypt(data, depotKey)
//            val decrypted = aes.doFinal(data, iv.size, data.size - iv.size)

            writtenDecompressed = if (buffer.size > 1 && buffer[0] == 'V'.code.toByte() && buffer[1] == 'Z'.code.toByte()) {
                logger.debug("Decompressing chunk ${Utils.encodeHexString(info.chunkID)} with VZipUtil")
                MemoryStream(buffer, 0, bytesWrittenToBuffer).use { ms ->
//                MemoryStream(buffer).use { ms ->
                    VZipUtil.decompress(ms, destination, verifyChecksum = false)
                }
            } else {
                logger.debug("Decompressing chunk ${Utils.encodeHexString(info.chunkID)} with ZipUtil")
//                MemoryStream(buffer).use { ms ->
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
//        val dataCrc = Adler32().run {
//            update(destination.sliceArray(0 until writtenDecompressed))
//            value.toInt()
//        }

        if(dataCrc != info.checksum) {
//            FileOutputStream(File("/data/data/com.OxGames.Pluvia/${Utils.encodeHexString(info.chunkID)}_fail.zip")).use { stream ->
//                stream.write(buffer, 0, bytesWrittenToBuffer)
//            }
            throw IOException("Processed data checksum is incorrect ($dataCrc != ${info.checksum})! Downloaded depot chunk is corrupt or invalid/wrong depot key?")
//            logger.error("Processed data checksum is incorrect ($dataCrc != ${info.checksum})! Downloaded depot chunk is corrupt or invalid/wrong depot key?")
        } else {
//            FileOutputStream(File("/data/data/com.OxGames.Pluvia/${Utils.encodeHexString(info.chunkID)}_success.zip")).use { stream ->
//                stream.write(buffer, 0, bytesWrittenToBuffer)
//            }
        }

        return writtenDecompressed
    }
}
