package `in`.dragonbra.javasteam.util.compat

import java.io.IOException
import java.io.InputStream
import java.util.Objects
import kotlin.jvm.Throws
import kotlin.math.min

/**
 * Compatibility (extension) functions for [InputStream.readNBytes].
 * These are basically the same from InputStream.
 */

@Throws(IOException::class)
fun InputStream.readNBytesCompat(b: ByteArray, off: Int, len: Int): Int {
    Objects.checkFromIndexSize(off, len, b.size)

    var n = 0

    while (n < len) {
        val count = read(b, off + n, len - n)
        if (count < 0) {
            break
        }
        n += count
    }

    return n
}

@Suppress("RedundantExplicitType")
@Throws(IOException::class)
fun InputStream.readNBytesCompat(len: Int): ByteArray {
    if (len < 0) {
        throw IllegalArgumentException("len < 0")
    }

    var bufs: MutableList<ByteArray>? = null
    var result: ByteArray? = null
    var total: Int = 0
    var remaining: Int = len
    var n: Int

    do {
        val buf = ByteArray(min(remaining, 8192))
        var nread = 0

        // read to EOF which may read more or less than buffer size
        while (read(buf, nread, minOf(buf.size - nread, remaining)).also { n = it } > 0) {
            nread += n
            remaining -= n
        }

        if (nread > 0) {
            if ((Integer.MAX_VALUE - 8) - total < nread) {
                throw OutOfMemoryError("Required array size too large")
            }
            total += nread
            if (result == null) {
                result = buf
            } else {
                if (bufs == null) {
                    bufs = arrayListOf()
                    bufs.add(result)
                }
                bufs.add(buf)
            }
        }
        // if the last call to read returned -1 or the number of bytes
        // requested have been read then break
    } while (n >= 0 && remaining > 0)

    if (bufs == null) {
        if (result == null) {
            return ByteArray(0)
        }
        return if (result.size == total) result else result.copyOf(total)
    }

    result = ByteArray(total)
    var offset = 0
    remaining = total

    bufs.forEach { b ->
        val count = min(b.size, remaining)
        System.arraycopy(b, 0, result, offset, count)
        offset += count
        remaining -= count
    }

    return result
}
