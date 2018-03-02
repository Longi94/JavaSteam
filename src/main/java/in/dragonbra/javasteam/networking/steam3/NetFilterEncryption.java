package in.dragonbra.javasteam.networking.steam3;

import in.dragonbra.javasteam.util.crypto.CryptoException;
import in.dragonbra.javasteam.util.crypto.CryptoHelper;
import in.dragonbra.javasteam.util.log.LogManager;
import in.dragonbra.javasteam.util.log.Logger;

/**
 * @author lngtr
 * @since 2018-02-24
 */
public class NetFilterEncryption implements INetFilterEncryption {

    private static final Logger logger = LogManager.getLogger(NetFilterEncryption.class);

    private final byte[] sessionKey;

    public NetFilterEncryption(byte[] sessionKey) {
        if (sessionKey.length != 32) {
            logger.debug("AES session key was not 32 bytes!");
        }
        this.sessionKey = sessionKey;
    }

    @Override
    public byte[] processIncoming(byte[] data) {
        try {
            return CryptoHelper.symmetricDecrypt(data, sessionKey);
        } catch (CryptoException e) {
            throw new IllegalStateException("Unable to decrypt incoming packet", e);
        }
    }

    @Override
    public byte[] processOutgoing(byte[] data) {
        try {
            return CryptoHelper.symmetricEncrypt(data, sessionKey);
        } catch (CryptoException e) {
            throw new IllegalStateException("Unable to encrypt outgoing packet", e);
        }
    }
}
