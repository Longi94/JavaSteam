package `in`.dragonbra.javasteam.networking.steam3

import `in`.dragonbra.javasteam.util.crypto.CryptoException
import `in`.dragonbra.javasteam.util.crypto.CryptoHelper
import `in`.dragonbra.javasteam.util.log.LogManager.getLogger
import java.io.IOException

/**
 * @author lngtr
 * @since 2018-02-24
 */
class NetFilterEncryption(private val sessionKey: ByteArray) : INetFilterEncryption {
    init {
        if (sessionKey.size != 32) {
            logger.debug("AES session key was not 32 bytes!")
        }
    }

    @Throws(IOException::class)
    override fun processIncoming(data: ByteArray): ByteArray {
        // JavaSteam Addition - Short Circuit.
        if (data.size < 16) {
            throw IOException("Encrypted packet is too short")
        }

        try {
            return CryptoHelper.symmetricDecrypt(data, sessionKey)
        } catch (e: CryptoException) {
            throw IOException("Unable to decrypt incoming packet", e)
        }
    }

    override fun processOutgoing(data: ByteArray): ByteArray = try {
        return CryptoHelper.symmetricEncrypt(data, sessionKey)
    } catch (e: CryptoException) {
        throw IllegalStateException("Unable to encrypt outgoing packet", e)
    }

    companion object {
        private val logger = getLogger(NetFilterEncryption::class.java)
    }
}
