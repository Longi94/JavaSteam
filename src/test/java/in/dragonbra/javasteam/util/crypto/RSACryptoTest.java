package in.dragonbra.javasteam.util.crypto;

import in.dragonbra.javasteam.enums.EUniverse;
import in.dragonbra.javasteam.util.KeyDictionary;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

public class RSACryptoTest {

    private RSACrypto rsaCrypto;

    @BeforeEach
    public void setUp() {
        var pubKey = KeyDictionary.getPublicKey(EUniverse.Public);
        rsaCrypto = new RSACrypto(pubKey);
    }

    @Test
    public void encrypt() {
        var input = CryptoHelper.generateRandomBlock(32);
        var encrypted = rsaCrypto.encrypt(input);

        Assertions.assertNotNull(encrypted);
    }

    @Test
    public void cipherInstance() throws NoSuchAlgorithmException, NoSuchPaddingException, NoSuchProviderException {
        var cipher = Cipher.getInstance("RSA/None/OAEPWithSHA1AndMGF1Padding", CryptoHelper.SEC_PROV);
        Assertions.assertNotNull(cipher);
    }
}
