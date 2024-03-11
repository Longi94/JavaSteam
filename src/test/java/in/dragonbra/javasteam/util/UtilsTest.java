package in.dragonbra.javasteam.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
        assertEquals(0x0967B587, result);
    }
}
