package in.dragonbra.javasteam.util.crypto;

import in.dragonbra.javasteam.TestBase;
import in.dragonbra.javasteam.enums.EUniverse;
import in.dragonbra.javasteam.util.KeyDictionary;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

public class AsnKeyParserTest extends TestBase {

    private static final byte[] KEY = KeyDictionary.getPublicKey(EUniverse.Public);

    private static final byte[] EXPECTED_EXP = new byte[]{0x11};

    private static final byte[] EXPECTED_MOD = new byte[]{ (byte) 0x00,
            (byte) 0xdf, (byte) 0xec, (byte) 0x1a, (byte) 0xd6, (byte) 0x2c, (byte) 0x10, (byte) 0x66, (byte) 0x2c,
            (byte) 0x17, (byte) 0x35, (byte) 0x3a, (byte) 0x14, (byte) 0xb0, (byte) 0x7c, (byte) 0x59, (byte) 0x11,
            (byte) 0x7f, (byte) 0x9d, (byte) 0xd3, (byte) 0xd8, (byte) 0x2b, (byte) 0x7a, (byte) 0xe3, (byte) 0xe0,
            (byte) 0x15, (byte) 0xcd, (byte) 0x19, (byte) 0x1e, (byte) 0x46, (byte) 0xe8, (byte) 0x7b, (byte) 0x87,
            (byte) 0x74, (byte) 0xa2, (byte) 0x18, (byte) 0x46, (byte) 0x31, (byte) 0xa9, (byte) 0x03, (byte) 0x14,
            (byte) 0x79, (byte) 0x82, (byte) 0x8e, (byte) 0xe9, (byte) 0x45, (byte) 0xa2, (byte) 0x49, (byte) 0x12,
            (byte) 0xa9, (byte) 0x23, (byte) 0x68, (byte) 0x73, (byte) 0x89, (byte) 0xcf, (byte) 0x69, (byte) 0xa1,
            (byte) 0xb1, (byte) 0x61, (byte) 0x46, (byte) 0xbd, (byte) 0xc1, (byte) 0xbe, (byte) 0xbf, (byte) 0xd6,
            (byte) 0x01, (byte) 0x1b, (byte) 0xd8, (byte) 0x81, (byte) 0xd4, (byte) 0xdc, (byte) 0x90, (byte) 0xfb,
            (byte) 0xfe, (byte) 0x4f, (byte) 0x52, (byte) 0x73, (byte) 0x66, (byte) 0xcb, (byte) 0x95, (byte) 0x70,
            (byte) 0xd7, (byte) 0xc5, (byte) 0x8e, (byte) 0xba, (byte) 0x1c, (byte) 0x7a, (byte) 0x33, (byte) 0x75,
            (byte) 0xa1, (byte) 0x62, (byte) 0x34, (byte) 0x46, (byte) 0xbb, (byte) 0x60, (byte) 0xb7, (byte) 0x80,
            (byte) 0x68, (byte) 0xfa, (byte) 0x13, (byte) 0xa7, (byte) 0x7a, (byte) 0x8a, (byte) 0x37, (byte) 0x4b,
            (byte) 0x9e, (byte) 0xc6, (byte) 0xf4, (byte) 0x5d, (byte) 0x5f, (byte) 0x3a, (byte) 0x99, (byte) 0xf9,
            (byte) 0x9e, (byte) 0xc4, (byte) 0x3a, (byte) 0xe9, (byte) 0x63, (byte) 0xa2, (byte) 0xbb, (byte) 0x88,
            (byte) 0x19, (byte) 0x28, (byte) 0xe0, (byte) 0xe7, (byte) 0x14, (byte) 0xc0, (byte) 0x42, (byte) 0x89};

    @Test
    public void parseRSAPublicKey() throws BerDecodeException {
        final List<Byte> list = new ArrayList<>();
        for (final byte b : KEY) {
            list.add(b);
        }
        final AsnKeyParser keyParser = new AsnKeyParser(list);
        final BigInteger[] keys = keyParser.parseRSAPublicKey();

        byte[] exp = keys[1].toByteArray();
        byte[] mod = keys[0].toByteArray();

        assertArrayEquals(EXPECTED_EXP, exp);
        assertArrayEquals(EXPECTED_MOD, mod);
    }
}