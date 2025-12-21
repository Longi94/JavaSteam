package `in`.dragonbra.javasteam.util.compat

import java.io.ByteArrayOutputStream
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets

/**
 * Compatibility class to provide compatibility with Java ByteArrayOutputStream.
 *
 * @author Lossy
 * @since 30/12/2024
 */
object ByteArrayOutputStreamCompat {

    /**
     * Converts ByteArrayOutputStream to String using UTF-8 encoding.
     * Compatible with all Android API levels.
     * @param byteArrayOutputStream the stream to convert
     * @return UTF-8 decoded string
     */
    @JvmStatic
    fun toString(byteArrayOutputStream: ByteArrayOutputStream): String = toString(byteArrayOutputStream, StandardCharsets.UTF_8)

    /**
     * Converts ByteArrayOutputStream to String using specified charset.
     * Compatible with all Android API levels.
     *
     * @param byteArrayOutputStream the stream to convert
     * @param charset the charset to use for decoding
     * @return decoded string
     */
    @JvmStatic
    fun toString(byteArrayOutputStream: ByteArrayOutputStream, charset: Charset): String {
        val bytes = byteArrayOutputStream.toByteArray()
        return String(bytes, 0, byteArrayOutputStream.size(), charset)
    }
}
