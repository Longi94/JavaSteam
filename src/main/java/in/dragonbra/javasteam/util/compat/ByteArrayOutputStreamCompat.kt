package `in`.dragonbra.javasteam.util.compat

import java.io.ByteArrayOutputStream

/**
 * Compatibility class to provide compatibility with Java ByteArrayOutputStream.
 *
 * @author Lossy
 * @since 30/12/2024
 */
object ByteArrayOutputStreamCompat {

    @JvmStatic
    fun toString(byteArrayOutputStream: ByteArrayOutputStream): String =
        String(byteArrayOutputStream.toByteArray(), 0, byteArrayOutputStream.size())
}
