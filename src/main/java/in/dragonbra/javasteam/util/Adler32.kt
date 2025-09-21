package `in`.dragonbra.javasteam.util

// See https://www.rfc-editor.org/rfc/rfc1950.html
object Adler32 {

    // Largest prime smaller than 65536
    private const val BASE = 65521

    // NMAX is the largest n such that 255n(n+1)/2 + (n+1)(BASE-1) <= 2^32-1
    private const val NMAX = 5552

    /**
     * Calculates the Adler32 checksum with the bytes taken from the span using zero as the initial seed.
     */
    @JvmStatic
    fun calculate(buffer: ByteArray): Int = calculate(0, buffer)

    /**
     * Calculates the Adler32 checksum with the bytes taken from the span.
     * @param adler The input Adler32 value.
     * @param buffer The readonly span of bytes.
     * @return The [Int].
     */
    @JvmStatic
    fun calculate(adler: Int, buffer: ByteArray): Int {
        var s1 = adler and 0xFFFF
        var s2 = (adler ushr 16) and 0xFFFF
        var pos = 0

        while (pos < buffer.size) {
            val len = minOf(buffer.size - pos, NMAX)
            val end = pos + len

            while (pos < end) {
                s1 += buffer[pos].toInt() and 0xFF
                s2 += s1
                pos++
            }

            s1 %= BASE
            s2 %= BASE
        }

        return (s2 shl 16) or s1
    }
}
