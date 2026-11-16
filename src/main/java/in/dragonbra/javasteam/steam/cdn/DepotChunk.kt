package `in`.dragonbra.javasteam.steam.cdn

import `in`.dragonbra.javasteam.types.ChunkData
import `in`.dragonbra.javasteam.util.Adler32
import `in`.dragonbra.javasteam.util.JavaSteamAddition
import `in`.dragonbra.javasteam.util.Strings
import `in`.dragonbra.javasteam.util.VZipUtil
import `in`.dragonbra.javasteam.util.VZstdUtil
import `in`.dragonbra.javasteam.util.ZipUtil
import `in`.dragonbra.javasteam.util.crypto.CryptoHelper
import `in`.dragonbra.javasteam.util.stream.MemoryStream
import org.apache.commons.lang3.SystemUtils
import java.io.IOException
import java.security.Provider
import java.security.Security
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * Provides a helper function to decrypt and decompress a single depot chunk.
 */
object DepotChunk {

    /**
     * The provider chunks are decrypted with: the platform's own, which uses the AES instructions of
     * the CPU where it has them. Bouncy Castle's AES is plain Java and several times slower, which
     * matters here because every byte of a download passes through it. It is only the fallback, for a
     * runtime that does not have the platform provider.
     *
     * The provider is named because [CryptoHelper] puts Bouncy Castle first in the provider list on
     * Android, so a lookup without one would still pick it there.
     */
    @JavaSteamAddition
    private val aesProvider: Provider by lazy {
        val platform = if (SystemUtils.IS_OS_ANDROID) "AndroidOpenSSL" else "SunJCE"

        Security.getProvider(platform)
            ?: Security.getProvider(CryptoHelper.SEC_PROV)
            ?: throw IllegalStateException(
                "No security provider to decrypt depot chunks with: " +
                    "neither $platform nor ${CryptoHelper.SEC_PROV} is registered."
            )
    }

    /**
     * Processes the specified depot key by decrypting the data with the given depot encryption key, and then by decompressing the data.
     * If the chunk has already been processed, this function does nothing.
     *
     * The chunk is decrypted in place: the contents of [data] are overwritten and must not be used afterwards.
     * @param info The depot chunk data representing.
     * @param data The encrypted chunk data. Overwritten by this call.
     * @param destination The buffer to receive the decrypted chunk data.
     * @param depotKey The depot decryption key.
     * @exception IOException Thrown if the processed data does not match the expected checksum given in its chunk information.
     * @exception IllegalArgumentException Thrown if the destination size is too small or the depot key is not 32 bytes long
     */
    @JvmStatic
    fun process(
        info: ChunkData,
        data: ByteArray,
        destination: ByteArray,
        depotKey: ByteArray,
    ): Int = process(info, data, data.size, destination, depotKey)

    /**
     * Processes the specified depot key by decrypting the data with the given depot encryption key, and then by decompressing the data.
     * This overload is for a [data] buffer that is longer than the chunk in it, such as a pooled one.
     *
     * The chunk is decrypted in place: the first [length] bytes of [data] are overwritten and must not be used afterwards.
     * @param info The depot chunk data representing.
     * @param data The buffer holding the encrypted chunk data, starting at index 0. Overwritten by this call.
     * @param length The number of bytes of [data] that hold the encrypted chunk.
     * @param destination The buffer to receive the decrypted chunk data.
     * @param depotKey The depot decryption key.
     * @exception IOException Thrown if the processed data does not match the expected checksum given in its chunk information.
     * @exception IllegalArgumentException Thrown if the destination size is too small, [length] is outside [data],
     * or the depot key is not 32 bytes long
     */
    @JavaSteamAddition
    @JvmStatic
    fun process(
        info: ChunkData,
        data: ByteArray,
        length: Int,
        destination: ByteArray,
        depotKey: ByteArray,
    ): Int = process(info, data, length, destination, depotKey, aesProvider)

    /** The implementation, with the provider as a parameter so tests can run it on the fallback provider too. */
    @JvmStatic
    @JvmName("processWithProvider")
    internal fun process(
        info: ChunkData,
        data: ByteArray,
        length: Int,
        destination: ByteArray,
        depotKey: ByteArray,
        provider: Provider,
    ): Int {
        require(destination.size >= info.uncompressedLength) {
            "The destination buffer must be longer than the chunk ${ChunkData::uncompressedLength.name}."
        }

        require(depotKey.size == 32) { "Tried to decrypt depot chunk with non 32 byte key!" }

        // first 16 bytes of input is the ECB encrypted IV
        val iv = ByteArray(16)

        require(length in iv.size..data.size) {
            "length ($length) must be between ${iv.size} and the size of the data buffer (${data.size})."
        }

        val keySpec = SecretKeySpec(depotKey, "AES")
        val ecbCipher = Cipher.getInstance("AES/ECB/NoPadding", provider)
        ecbCipher.init(Cipher.DECRYPT_MODE, keySpec)

        val ivBytesRead = ecbCipher.doFinal(data, 0, iv.size, iv)

        require(iv.size == ivBytesRead) { "Failed to decrypt depot chunk iv (${iv.size} != $ivBytesRead)" }

        // PKCS5Padding is the name every provider knows; for AES it is the same padding as PKCS7.
        val aes = Cipher.getInstance("AES/CBC/PKCS5Padding", provider)
        aes.init(Cipher.DECRYPT_MODE, keySpec, IvParameterSpec(iv))

        // Decrypted in place instead of into a second array per chunk: the output starts exactly
        // where the input does, right after the IV. With CBC and padding the decrypted size is
        // always smaller than the input.
        //
        // The output must not be moved to index 0, 16 bytes behind the input. Cipher.doFinal is
        // documented as copy-safe, but Android's provider (Conscrypt) hands both ranges straight to
        // BoringSSL, which only supports an output that is the input exactly or does not overlap it
        // at all. A shifted overlap may work on one CPU's AES code path and corrupt on another's.
        val buffer = data
        val start = iv.size

        val writtenDecompressed: Int

        try {
            val written = aes.doFinal(data, start, length - start, buffer, start)

            // Per SK:
            //  Steam client checks for like 20 bytes for pkzip, and 22 bytes for vzip,
            //  I'm just being safe and checking for a smaller value.
            if (written < 16) {
                throw IOException("Not enough data in the decrypted depot chunk (was $written bytes).")
            }

            if (buffer[start] == 'V'.code.toByte() &&
                buffer[start + 1] == 'S'.code.toByte() &&
                buffer[start + 2] == 'Z'.code.toByte() &&
                buffer[start + 3] == 'a'.code.toByte()
            ) {
                // Zstd. VZstdUtil reads from index 0, so the decrypted data is moved down first;
                // System.arraycopy is safe for ranges that overlap.
                System.arraycopy(buffer, start, buffer, 0, written)

                writtenDecompressed = VZstdUtil.decompress(
                    buffer = buffer,
                    destination = destination,
                    verifyChecksum = false,
                    length = written,
                )
            } else if (buffer[start] == 'V'.code.toByte() &&
                buffer[start + 1] == 'Z'.code.toByte() &&
                buffer[start + 2] == 'a'.code.toByte()
            ) {
                // LZMA
                MemoryStream(buffer, start, written).use { decryptedStream ->
                    writtenDecompressed = VZipUtil.decompress(
                        ms = decryptedStream,
                        destination = destination,
                        verifyChecksum = false
                    )
                }
            } else if (buffer[start] == 'P'.code.toByte() &&
                buffer[start + 1] == 'K'.code.toByte() &&
                buffer[start + 2].toInt() == 0x03 &&
                buffer[start + 3].toInt() == 0x04
            ) {
                // Per SK:
                //  Steam client code performs the same check.

                // PKzip
                MemoryStream(buffer, start, written).use { decryptedStream ->
                    writtenDecompressed = ZipUtil.decompress(
                        ms = decryptedStream,
                        destination = destination,
                        verifyChecksum = false
                    )
                }
            } else {
                throw IOException(
                    "Unexpected depot chunk compression " +
                        "(first four bytes are ${Strings.toHex(buffer.copyOfRange(start, start + 4))})."
                )
            }
        } catch (e: Exception) {
            val chunkID = info.chunkID?.let { Strings.toHex(it) } ?: "unknown"
            throw IOException("Failed to decompress chunk $chunkID: $e\n${e.stackTraceToString()}")
        }

        if (info.uncompressedLength != writtenDecompressed) {
            throw IOException(
                "Processed data checksum failed to decompress to the expected chunk uncompressed length. " +
                    "(was $writtenDecompressed, should be ${info.uncompressedLength})"
            )
        }

        val dataCrc = Adler32.calculate(0, destination, 0, writtenDecompressed)

        if (dataCrc != info.checksum) {
            throw IOException("Processed data checksum is incorrect ($dataCrc != ${info.checksum})! Downloaded depot chunk is corrupt or invalid/wrong depot key?")
        }

        return writtenDecompressed
    }
}
