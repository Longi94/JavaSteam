package in.dragonbra.javasteam.util;

import in.dragonbra.javasteam.enums.EOSType;
import org.apache.commons.lang3.SystemUtils;

import java.util.zip.CRC32;
import java.util.zip.Checksum;

/**
 * @author lngtr
 * @since 2018-02-23
 */
public class Utils {

    private static final String JAVA_RUNTIME = getSystemProperty("java.runtime.name");

    public static EOSType getOSType() {
        if (SystemUtils.IS_OS_WINDOWS_7) {
            return EOSType.Windows7;
        }
        if (SystemUtils.IS_OS_WINDOWS_8) {
            return EOSType.Windows8;
        }
        if (SystemUtils.IS_OS_WINDOWS_10) {
            return EOSType.Windows10;
        }
        if (SystemUtils.IS_OS_WINDOWS_95) {
            return EOSType.Win95;
        }
        if (SystemUtils.IS_OS_WINDOWS_98) {
            return EOSType.Win98;
        }
        if (SystemUtils.IS_OS_WINDOWS_2000) {
            return EOSType.Win2000;
        }
        if (SystemUtils.IS_OS_WINDOWS_2003) {
            return EOSType.Win2003;
        }
        if (SystemUtils.IS_OS_WINDOWS_2008) {
            return EOSType.Win2008;
        }
        if (SystemUtils.IS_OS_WINDOWS_2012) {
            return EOSType.Win2012;
        }
        if (SystemUtils.IS_OS_WINDOWS_ME) {
            return EOSType.WinME;
        }
        if (SystemUtils.IS_OS_WINDOWS_NT) {
            return EOSType.WinNT;
        }
        if (SystemUtils.IS_OS_WINDOWS_VISTA) {
            return EOSType.WinVista;
        }
        if (SystemUtils.IS_OS_WINDOWS_XP) {
            return EOSType.WinXP;
        }
        if (SystemUtils.IS_OS_WINDOWS) {
            return EOSType.WinUnknown;
        }
        if (SystemUtils.IS_OS_MAC_OSX_TIGER) {
            return EOSType.MacOS104;
        }
        if (SystemUtils.IS_OS_MAC_OSX_LEOPARD) {
            return EOSType.MacOS105;
        }
        if (SystemUtils.IS_OS_MAC_OSX_SNOW_LEOPARD) {
            return EOSType.MacOS106;
        }
        if (SystemUtils.IS_OS_MAC_OSX_LION) {
            return EOSType.MacOS107;
        }
        if (SystemUtils.IS_OS_MAC_OSX_MOUNTAIN_LION) {
            return EOSType.MacOS108;
        }
        if (SystemUtils.IS_OS_MAC_OSX_MAVERICKS) {
            return EOSType.MacOS109;
        }
        if (SystemUtils.IS_OS_MAC_OSX_YOSEMITE) {
            return EOSType.MacOS1010;
        }
        if (SystemUtils.IS_OS_MAC_OSX_EL_CAPITAN) {
            return EOSType.MacOS1011;
        }
        if (checkOS("Mac OS X", "10.12")) {
            return EOSType.MacOS1012;
        }
        if (checkOS("Mac OS X", "10.13")) {
            return EOSType.Macos1013;
        }
        if (checkOS("Mac OS X", "10.14")) {
            return EOSType.Macos1014;
        }
        if (SystemUtils.IS_OS_MAC) {
            return EOSType.MacOSUnknown;
        }
        if (JAVA_RUNTIME != null && JAVA_RUNTIME.startsWith("Android")) {
            return EOSType.AndroidUnknown;
        }
        if (SystemUtils.IS_OS_LINUX) {
            return EOSType.LinuxUnknown;
        }
        return EOSType.Unknown;
    }

    private static boolean checkOS(String namePrefix, String versionPrefix) {
        return SystemUtils.OS_NAME.startsWith(namePrefix) && SystemUtils.OS_VERSION.startsWith(versionPrefix);
    }

    private static String getSystemProperty(final String property) {
        try {
            return System.getProperty(property);
        } catch (final SecurityException ex) {
            // we are not allowed to look at this property
            return null;
        }
    }

    /**
     * Convenience method for calculating the CRC2 checksum of a string.
     *
     * @param s the string
     * @return long value of the CRC32
     */
    public static long crc32(String s) {
        Checksum checksum = new CRC32();
        byte[] bytes = s.getBytes();
        checksum.update(bytes, 0, bytes.length);
        return checksum.getValue();
    }
}
