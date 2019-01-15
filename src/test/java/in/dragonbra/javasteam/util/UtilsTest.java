package in.dragonbra.javasteam.util;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author lngtr
 * @since 2019-01-15
 */
public class UtilsTest {

    @Test
    public void crc32() {
        long result = Utils.crc32("test_string");
        assertEquals(0x0967B587, result);
    }
}