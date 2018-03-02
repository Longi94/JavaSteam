package in.dragonbra.javasteam.networking.steam3;

import in.dragonbra.javasteam.util.crypto.CryptoException;
import in.dragonbra.javasteam.util.crypto.CryptoHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author lngtr
 * @since 2018-02-24
 */
public class NetFilterEncryptionWithHMAC implements INetFilterEncryption {

    private static final Logger logger = LogManager.getLogger(NetFilterEncryptionWithHMAC.class);

    private final byte[] sessionKey;
    private final byte[] hmacSecret;

    public NetFilterEncryptionWithHMAC(byte[] sessionKey) {
        if (sessionKey.length != 32) {
            logger.debug("AES session key was not 32 bytes!");
        }
        this.sessionKey = sessionKey;
        this.hmacSecret = new byte[16];
        System.arraycopy(sessionKey, 0, hmacSecret, 0, hmacSecret.length);
    }

    @Override
    public byte[] processIncoming(byte[] data) {
        try {
            return CryptoHelper.symmetricDecryptHMACIV(data, sessionKey, hmacSecret);
        } catch (CryptoException e) {
            throw new IllegalStateException("Unable to decrypt incoming packet", e);
        }
    }

    @Override
    public byte[] processOutgoing(byte[] data) {
        try {
            return CryptoHelper.symmetricEncryptWithHMACIV(data, sessionKey, hmacSecret);
        } catch (CryptoException e) {
            throw new IllegalStateException("Unable to encrypt outgoing packet", e);
        }
    }
}
