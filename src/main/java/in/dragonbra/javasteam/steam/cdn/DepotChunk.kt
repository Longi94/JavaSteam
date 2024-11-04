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
        logger.debug("${Utils.encodeHexString(info.chunkID)} is using depot key [${depotKey.map { it.toUByte() }.joinToString(", ")}]")

        // first 16 bytes of input is the ECB encrypted IV
//        val keySpec = SecretKeySpec(depotKey, "AES")
        val ecbCipher = Cipher.getInstance("AES/ECB/NoPadding", CryptoHelper.SEC_PROV)
        ecbCipher.init(Cipher.DECRYPT_MODE, SecretKeySpec(depotKey, "AES"))
        val iv = ByteArray(16)
        val ivBytesRead = ecbCipher.doFinal(data, 0, iv.size, iv)
        require(iv.size == ivBytesRead) { "Failed to decrypt depot chunk iv (${iv.size} != $ivBytesRead)" }
        logger.debug("${Utils.encodeHexString(info.chunkID)} iv is [${iv.map { it.toUByte() }.joinToString(", ")}]")

        // Prepare the data for CBC decryption
//        val dataToDecrypt = data.sliceArray(iv.size until data.size)

        // With CBC and padding, the decrypted size will always be smaller
        val buffer = ByteArray(data.size - iv.size)
        val cbcCipher = Cipher.getInstance("AES/CBC/PKCS7Padding", CryptoHelper.SEC_PROV)
        cbcCipher.init(Cipher.DECRYPT_MODE, SecretKeySpec(depotKey, "AES"), IvParameterSpec(iv))
//        val outputSize = aes.getOutputSize(data.size - iv.size)
//        val outputSize = data.size - iv.size
//        val buffer = ByteArray(outputSize)
        var bytesWrittenToBuffer: Int = 0

        val writtenDecompressed: Int
        val isVzip: Boolean
//        val decrypted: ByteArray
//        val buffer: ByteArray

        try {
            logger.debug("Expected aes output size of chunk ${Utils.encodeHexString(info.chunkID)} is ${cbcCipher.getOutputSize(data.size - iv.size)} for input size of ${data.size - iv.size}")
//            val cipherData = ByteArray(data.size - iv.size)
//            System.arraycopy(data, iv.size, cipherData, 0, cipherData.size)
//            decrypted = aes.doFinal(cipherData)
//            val written = aes.doFinal(cipherData, 0, cipherData.size, buffer)
            bytesWrittenToBuffer = cbcCipher.doFinal(data, iv.size, data.size - iv.size, buffer)
//            bytesWrittenToBuffer = aes.update(data, iv.size, data.size - iv.size, buffer)
//            logger.debug("Actually written bytes after doFinal of chunk ${Utils.encodeHexString(info.chunkID)} is $bytesWrittenToBuffer")
//            logger.debug("Decrypting chunk ${Utils.encodeHexString(info.chunkID)} with depot key: [${depotKey.joinToString(", ")}] (${depotKey.size})")
//            val decrypted = CryptoHelper.symmetricDecrypt(data, depotKey)
//            val decrypted = aes.doFinal(data, iv.size, data.size - iv.size)
//            val blockSize = 16
//            val tempBuffer = ByteArray(blockSize)
//            while (bytesWrittenToBuffer < outputSize) {
//                val bytesToProcess = minOf(blockSize, outputSize - bytesWrittenToBuffer)
//                val written = cbcCipher.update(data, iv.size + bytesWrittenToBuffer, bytesToProcess, tempBuffer)
//                System.arraycopy(tempBuffer, 0, buffer, bytesWrittenToBuffer, written)
//                bytesWrittenToBuffer += written
//            }
//            // First update with all blocks except the last one
//            val blockSize = 16
//            val fullBlocksLength = dataToDecrypt.size - (dataToDecrypt.size % blockSize)
//            if (fullBlocksLength > 0) {
//                bytesWrittenToBuffer = cbcCipher.update(dataToDecrypt, 0, fullBlocksLength, buffer)
//            }
//
//            // Then do final with the last block (which includes padding)
//            val remainingBytes = dataToDecrypt.size - fullBlocksLength
//            val finalBytes = cbcCipher.doFinal(
//                dataToDecrypt,
//                fullBlocksLength,
//                remainingBytes
//            )
//            // Copy final bytes to buffer
//            System.arraycopy(finalBytes, 0, buffer, bytesWrittenToBuffer, finalBytes.size)
//            bytesWrittenToBuffer += finalBytes.size
//            val blockSize = 256
//            val block = ByteArray(blockSize)
//            var bytes: Int
//            val mutableBuffer = mutableListOf<Byte>()
//            do {
//                bytes = 0
//                try {
//                    bytes = cbcCipher.update(data, iv.size + bytesWrittenToBuffer, blockSize, block)
//                    bytesWrittenToBuffer += bytes
//                    mutableBuffer.addAll(block.toList())
//                } catch(e: Exception) {
//                    logger.error("${Utils.encodeHexString(info.chunkID)} reached end of encrypted data after $bytesWrittenToBuffer byte(s)")
//                }
//                if (bytes <= 0) {
//                    val lastBlock = cbcCipher.doFinal(data, iv.size + bytesWrittenToBuffer, data.size - (iv.size + bytesWrittenToBuffer))
//                    bytesWrittenToBuffer += lastBlock.size
//                    mutableBuffer.addAll(lastBlock.toList())
//                }
//            } while (bytes > 0)
//            buffer = ByteArray(bytesWrittenToBuffer)
//            System.arraycopy(mutableBuffer.toByteArray(), 0, buffer, 0, bytesWrittenToBuffer)

            writtenDecompressed = if (buffer.size > 1 && buffer[0] == 'V'.code.toByte() && buffer[1] == 'Z'.code.toByte()) {
//            writtenDecompressed = if (decrypted.size > 1 && decrypted[0] == 'V'.code.toByte() && decrypted[1] == 'Z'.code.toByte()) {
                isVzip = true
                logger.debug("Decompressing chunk ${Utils.encodeHexString(info.chunkID)} with VZipUtil")
//                info.uncompressedLength
                MemoryStream(buffer, 0, bytesWrittenToBuffer).use { ms ->
//                MemoryStream(decrypted).use { ms ->
                    VZipUtil.decompress(ms, destination, verifyChecksum = false)
                }
            } else {
                isVzip = false
                logger.debug("Decompressing chunk ${Utils.encodeHexString(info.chunkID)} with ZipUtil")
//                MemoryStream(buffer).use { ms ->
//                info.uncompressedLength
                MemoryStream(buffer, 0, bytesWrittenToBuffer).use { ms ->
//                MemoryStream(decrypted).use { ms ->
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

//        File("/data/data/com.OxGames.Pluvia/chunks/raw/").mkdirs()
//        File("/data/data/com.OxGames.Pluvia/chunks/decrypted/").mkdirs()
//        File("/data/data/com.OxGames.Pluvia/chunks/extracted/").mkdirs()
//        val ext = if(isVzip) "vzip" else "zip"
        if(dataCrc != info.checksum) {
//            FileOutputStream(File("/data/data/com.OxGames.Pluvia/chunks/raw/${Utils.encodeHexString(info.chunkID)}_fail.$ext.raw")).use { stream ->
//                stream.write(data)
//            }
//            FileOutputStream(File("/data/data/com.OxGames.Pluvia/chunks/decrypted/${Utils.encodeHexString(info.chunkID)}_fail.$ext")).use { stream ->
//                stream.write(buffer, 0, bytesWrittenToBuffer)
////                stream.write(decrypted)
//            }
//            FileOutputStream(File("/data/data/com.OxGames.Pluvia/chunks/extracted/${Utils.encodeHexString(info.chunkID)}_fail.$ext.bin")).use { stream ->
//                stream.write(destination, 0, writtenDecompressed)
//            }
            throw IOException("Processed data checksum is incorrect ($dataCrc != ${info.checksum})! Downloaded depot chunk is corrupt or invalid/wrong depot key?")
//            logger.error("Processed data checksum is incorrect ($dataCrc != ${info.checksum})! Downloaded depot chunk is corrupt or invalid/wrong depot key?")
        } else {
//            FileOutputStream(File("/data/data/com.OxGames.Pluvia/chunks/raw/${Utils.encodeHexString(info.chunkID)}_success$ext.raw")).use { stream ->
//                stream.write(data)
//            }
//            FileOutputStream(File("/data/data/com.OxGames.Pluvia/chunks/decrypted/${Utils.encodeHexString(info.chunkID)}_success.$ext")).use { stream ->
//                stream.write(buffer, 0, bytesWrittenToBuffer)
////                stream.write(decrypted)
//            }
//            FileOutputStream(File("/data/data/com.OxGames.Pluvia/chunks/extracted/${Utils.encodeHexString(info.chunkID)}_success.$ext.bin")).use { stream ->
//                stream.write(destination, 0, writtenDecompressed)
//            }
        }

        return writtenDecompressed
    }
}
