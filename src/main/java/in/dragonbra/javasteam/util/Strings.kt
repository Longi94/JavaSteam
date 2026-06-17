package `in`.dragonbra.javasteam.util

/**
 * Provides helper functions for null/empty string checks and hex string conversion.
 * @author lngtr
 * @since 2018-02-19
 */
object Strings {

    private val HEX_ARRAY = "0123456789ABCDEF".toCharArray()

    /**
     * Checks whether the given string is `null` or empty.
     * @param str the string to check.
     * @return `true` if [str] is `null` or empty, `false` otherwise.
     */
    @JvmStatic
    fun isNullOrEmpty(str: String?): Boolean = str == null || str.isEmpty()

    /**
     * Converts a byte array into its uppercase hexadecimal string representation.
     * @param bytes the bytes to encode.
     * @return a string twice the length of [bytes], containing its hex encoding.
     */
    @JvmStatic
    fun toHex(bytes: ByteArray): String {
        val hexChars = CharArray(bytes.size * 2)
        for (j in bytes.indices) {
            val v = bytes[j].toInt() and 0xFF
            hexChars[j * 2] = HEX_ARRAY[v ushr 4]
            hexChars[j * 2 + 1] = HEX_ARRAY[v and 0x0F]
        }
        return String(hexChars)
    }

    /**
     * Decodes a hexadecimal string into a byte array.
     * @param s the hex string to decode.
     * @return the decoded bytes.
     * @exception StringIndexOutOfBoundsException if [s] has an odd length.
     */
    @JvmStatic
    fun decodeHex(s: String): ByteArray {
        val data = ByteArray(s.length / 2)
        for (i in s.indices step 2) {
            data[i / 2] = ((Character.digit(s[i], 16) shl 4) + Character.digit(s[i + 1], 16)).toByte()
        }
        return data
    }
}
