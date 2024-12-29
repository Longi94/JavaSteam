package `in`.dragonbra.javasteam.util.compat

import java.io.ByteArrayOutputStream
import java.nio.charset.Charset

/**
 * Compatibility class to provide compatibility with Java ByteArrayOutputStream.
 */
object ByteArrayOutputStreamCompat {

    @JvmStatic
    fun toString(baos: ByteArrayOutputStream, charset: Charset): String = String(baos.toByteArray(), charset)
}
