package in.dragonbra.javasteam.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author lngtr
 * @since 2019-01-15
 */
public class UtilsTest {

    // Note: We can only set the "os.name" once, but "os.version" can be changed on the fly.
    // This breaks testing for EOSTypes.
    // If you create an EOSType test and run it individually after setting the properties above, it passes.

    @Test
    public void crc32() {
        long result = Utils.crc32("test_string");
        Assertions.assertEquals(0x0967B587, result);
    }

    @Test
    public void toHex() {
        byte[] byteArray = {(byte) 0xDE, (byte) 0xAD, (byte) 0xBE, (byte) 0xEF};
        Assertions.assertEquals("DEADBEEF", Strings.toHex(byteArray));
    }
    @Test
    public void decodeHex() {
        byte[] byteArray = {(byte) 0xDE, (byte) 0xAD, (byte) 0xBE, (byte) 0xEF};
        Assertions.assertArrayEquals(byteArray, Strings.decodeHex("deadbeef"));
    }
    @Test
    public void invalid_decodeHex() {
        Assertions.assertThrows(StringIndexOutOfBoundsException.class, () -> Strings.decodeHex("odd"));
    }
    @Test
    public void isNullOrEmpty() {
        Assertions.assertTrue(Strings.isNullOrEmpty(null));
        Assertions.assertTrue(Strings.isNullOrEmpty(""));
        Assertions.assertFalse(Strings.isNullOrEmpty("Hello World!"));
    }
}
