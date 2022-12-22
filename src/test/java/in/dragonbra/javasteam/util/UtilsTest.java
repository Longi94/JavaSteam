package in.dragonbra.javasteam.util;

import in.dragonbra.javasteam.enums.EOSType;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author lngtr
 * @since 2019-01-15
 */
public class UtilsTest {

    // TODO: getOSType() unit testing, mostly for proper linux identification

    @Test
    public void testEOSTypeForLinux_5_15() {
        System.setProperty("os.name", "Linux");
        System.setProperty("os.version", "5.15.79.1-microsoft-standard-WSL2");

        EOSType type = Utils.getOSType();
        System.out.println("os name: " + System.getProperty("os.name"));
        System.out.println("os version: " + System.getProperty("os.version"));
        assertEquals(EOSType.Linux5x, type);
    }

    @Test
    public void crc32() {
        long result = Utils.crc32("test_string");
        assertEquals(0x0967B587, result);
    }
}