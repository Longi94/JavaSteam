package `in`.dragonbra.javasteam.networking.steam3

import java.io.IOException

/**
 * @author lngtr
 * @since 2018-02-24
 */
interface INetFilterEncryption {
    @Throws(IOException::class)
    fun processIncoming(data: ByteArray): ByteArray
    fun processOutgoing(data: ByteArray): ByteArray
}
