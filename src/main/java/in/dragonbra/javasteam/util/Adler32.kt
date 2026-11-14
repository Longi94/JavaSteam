package `in`.dragonbra.javasteam.util

import java.util.zip.Adler32 as ZlibAdler32

/**
 * Adler-32 checksums (see [RFC 1950](https://www.rfc-editor.org/rfc/rfc1950.html)) with a seed of the caller's choice.
 *
 * Steam checksums depot chunks with a seed of 0, where standard Adler-32 (zlib) starts at 1.
 *
 * The bytes are summed by [java.util.zip.Adler32], which is native code or a JIT intrinsic on
 * every runtime and far faster than a loop written here. It can only start at 1, so the result is
 * moved to the requested seed afterwards, which is a few arithmetic operations.
 */
object Adler32 {

    /**
     * Base modulo value - largest prime smaller than 65536
     */
    private const val BASE = 65521

    /**
     * Calculates the Adler32 checksum of the whole [ByteArray] using 0 as the initial seed,
     * which is what Steam uses for depot chunks. This is not the standard Adler-32 value, which starts at 1.
     * @param buffer The byte array to process
     * @return The Adler-32 checksum, seeded with 0
     */
    @JvmStatic
    fun calculate(buffer: ByteArray): Int = calculate(0, buffer)

    /**
     * Calculates the Adler32 checksum with the bytes taken from the [ByteArray]
     * @param adler The input Adler32 value: 0 for a Steam depot chunk checksum, 1 for standard Adler-32,
     * or the result of an earlier call to continue that checksum with more data.
     * @param buffer The byte array to process
     * @return The updated Adler-32 checksum
     */
    @JvmStatic
    fun calculate(adler: Int, buffer: ByteArray): Int = calculate(adler, buffer, 0, buffer.size)

    /**
     * Calculates the Adler32 checksum over a range of the [ByteArray], without copying it.
     * @param adler The input Adler32 value: 0 for a Steam depot chunk checksum, 1 for standard Adler-32,
     * or the result of an earlier call to continue that checksum with more data.
     * @param buffer The byte array to process
     * @param offset Index of the first byte to process
     * @param length Number of bytes to process. With 0, [adler] is returned unchanged.
     * @return The updated Adler-32 checksum
     * @throws IllegalArgumentException if the range is outside [buffer]
     */
    @JvmStatic
    fun calculate(adler: Int, buffer: ByteArray, offset: Int, length: Int): Int {
        require(offset >= 0 && length >= 0 && offset <= buffer.size - length) {
            "Range (offset $offset, length $length) is outside the buffer (size ${buffer.size})."
        }

        // Nothing to add: the seed is the result, as before.
        if (length == 0) return adler

        // zlib's sum of the data alone. It always starts at 1.
        val zlib = ZlibAdler32()
        zlib.update(buffer, offset, length)
        val value = zlib.value

        // Move the sum from zlib's start (1, 0) to the seed that was passed in.
        // Over n bytes, the low half is seed1 + sum(bytes) and the high half is
        // seed2 + n * seed1 + (sum of the running low sums), both modulo BASE. zlib computed the
        // same with seed1 = 1 and seed2 = 0, so the difference is (seed1 - 1) in the low half
        // and seed2 + n * (seed1 - 1) in the high half. Every term below fits a Long easily.
        val seed1 = (adler and 0xFFFF).toLong()
        val seed2 = ((adler ushr 16) and 0xFFFF).toLong()

        val s1 = mod(seed1 + (value and 0xFFFF) - 1)
        val s2 = mod(seed2 + (value ushr 16) + (length % BASE) * mod(seed1 - 1))

        return ((s2 shl 16) or s1).toInt()
    }

    /** Remainder that is never negative. */
    private fun mod(x: Long): Long = ((x % BASE) + BASE) % BASE
}
