package `in`.dragonbra.javasteam.base

import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

/**
 * @author lngtr
 * @since 2018-02-21
 */
interface ISteamSerializable {
    @Throws(IOException::class)
    fun serialize(stream: OutputStream)

    @Throws(IOException::class)
    fun deserialize(stream: InputStream)
}
