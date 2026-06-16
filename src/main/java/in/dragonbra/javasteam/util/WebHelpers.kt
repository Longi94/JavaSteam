package `in`.dragonbra.javasteam.util

import java.nio.charset.StandardCharsets

/**
 * Provides helper functions for URL encoding.
 * @author lngtr
 * @since 2018-04-16
 */
object WebHelpers {

    private fun isUrlSafeChar(ch: Char): Boolean =
        ch in 'a'..'z' || ch in 'A'..'Z' || ch in '0'..'9' || ch == '-' || ch == '.' || ch == '_'

    /**
     * URL-encodes the given string, using UTF-8 to convert it to bytes first.
     * @param input the string to encode.
     * @return the URL-encoded string.
     */
    @JvmStatic
    fun urlEncode(input: String): String = urlEncode(input.toByteArray(StandardCharsets.UTF_8))

    /**
     * URL-encodes the given bytes.
     * @param input the bytes to encode.
     * @return the URL-encoded string.
     */
    @JvmStatic
    fun urlEncode(input: ByteArray): String {
        val encoded = StringBuilder(input.size * 2)

        for (i in input) {
            val inch = i.toInt().toChar()

            when {
                isUrlSafeChar(inch) -> encoded.append(inch)
                inch == ' ' -> encoded.append('+')
                else -> encoded.append("%%%02X".format(i))
            }
        }

        return encoded.toString()
    }
}
