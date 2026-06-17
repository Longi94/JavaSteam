package `in`.dragonbra.javasteam.util.crypto

import `in`.dragonbra.javasteam.util.log.LogManager.getLogger
import java.security.GeneralSecurityException
import java.security.KeyFactory
import java.security.spec.RSAPublicKeySpec
import javax.crypto.Cipher

/**
 * Handles encrypting and decrypting using the RSA public key encryption algorithm.
 */
class RSACrypto(key: ByteArray?) {

    companion object {
        private val logger = getLogger(RSACrypto::class.java)
    }

    private val cipher: Cipher? = try {
        requireNotNull(key) { "key is null" }
        val keys = AsnKeyParser(key.asList()).parseRSAPublicKey()
        val publicKeySpec = RSAPublicKeySpec(keys[0], keys[1])
        val factory = KeyFactory.getInstance("RSA")
        val rsaKey = factory.generatePublic(publicKeySpec)
        Cipher.getInstance("RSA/None/OAEPWithSHA1AndMGF1Padding", CryptoHelper.SEC_PROV).also {
            it.init(Cipher.ENCRYPT_MODE, rsaKey)
        }
    } catch (e: BerDecodeException) {
        logger.error(e)
        null
    } catch (e: GeneralSecurityException) {
        logger.debug(e)
        null
    }

    fun encrypt(input: ByteArray): ByteArray? = try {
        cipher?.doFinal(input)
    } catch (e: GeneralSecurityException) {
        logger.debug(e)
        null
    }
}
