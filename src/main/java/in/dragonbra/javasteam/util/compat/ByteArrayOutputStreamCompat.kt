package `in`.dragonbra.javasteam.util.compat

import java.io.ByteArrayOutputStream

/**
 * Compatibility class to provide compatibility with Java ByteArrayOutputStream.
 */
object ByteArrayOutputStreamCompat {

    @JvmStatic
    fun toString(byteArrayOutputStream: ByteArrayOutputStream): String =
        String(byteArrayOutputStream.toByteArray(), 0, byteArrayOutputStream.size())
}
