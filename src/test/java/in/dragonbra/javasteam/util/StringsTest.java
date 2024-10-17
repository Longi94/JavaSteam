package in.dragonbra.javasteam.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class StringsTest {

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
