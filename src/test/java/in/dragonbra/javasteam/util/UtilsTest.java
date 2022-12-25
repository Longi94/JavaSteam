package in.dragonbra.javasteam.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author lngtr
 * @since 2019-01-15
 */
public class UtilsTest {

    // TODO: Is it possible to actually test OS types on the fly?

//    @Before
//    public void init() {
//        System.setProperty("os.name", "Linux");
//    }
//
//    @Test
//    public void testEOSType_Linux_22() {
//        System.setProperty("os.version", "2.2");
//
//        EOSType type = Utils.getOSType();
//        assertEquals(EOSType.Linux22, type);
//    }
//
//    @Test
//    public void testEOSType_Linux_24() {
//        System.setProperty("os.version", "2.4");
//
//        EOSType type = Utils.getOSType();
//        assertEquals(EOSType.Linux24, type);
//    }
//
//    @Test
//    public void testEOSType_Linux_26() {
//        System.setProperty("os.version", "2.6.39");
//
//        EOSType type = Utils.getOSType();
//        assertEquals(EOSType.Linux26, type);
//    }
//
//    @Test
//    public void testEOSType_Linux_30() {
//        System.setProperty("os.version", "3.0");
//
//        EOSType type = Utils.getOSType();
//        assertEquals(EOSType.Linux3x, type);
//    }
//
//    @Test
//    public void testEOSType_Linux_310() {
//        System.setProperty("os.version", "3.10");
//
//        EOSType type = Utils.getOSType();
//        assertEquals(EOSType.Linux310, type);
//    }
//
//    @Test
//    public void testEOSType_Linux_318() {
//        System.setProperty("os.version", "3.18");
//
//        EOSType type = Utils.getOSType();
//        assertEquals(EOSType.Linux318, type);
//    }
//
//    @Test
//    public void testEOSType_Linux_40() {
//        System.setProperty("os.version", "4.0");
//
//        EOSType type = Utils.getOSType();
//        assertEquals(EOSType.Linux4x, type);
//    }
//
//    @Test
//    public void testEOSType_Linux_49() {
//        System.setProperty("os.version", "4.9");
//
//        EOSType type = Utils.getOSType();
//        assertEquals(EOSType.Linux49, type);
//    }
//
//    @Test
//    public void testEOSType_Linux_419() {
//        System.setProperty("os.version", "4.19");
//
//        EOSType type = Utils.getOSType();
//        assertEquals(EOSType.Linux419, type);
//    }
//
//    @Test
//    public void testEOSType_Linux_510() {
//        System.setProperty("os.version", "5.10");
//
//        EOSType type = Utils.getOSType();
//        assertEquals(EOSType.Linux510, type);
//    }
//
//    @Test
//    public void testEOSType_Linux_515() {
//        System.setProperty("os.version", "5.15.79.1-microsoft-standard-WSL2");
//
//        EOSType type = Utils.getOSType();
//        assertEquals(EOSType.Linux5x, type);
//    }
//
//    @Test
//    public void testEOSType_Linux_61() {
//        System.setProperty("os.version", "6.1");
//
//        EOSType type = Utils.getOSType();
//        assertEquals(EOSType.Linux6x, type);
//    }

    @Test
    public void crc32() {
        long result = Utils.crc32("test_string");
        assertEquals(0x0967B587, result);
    }
}