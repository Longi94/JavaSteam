package in.dragonbra.javasteam.util;

import in.dragonbra.javasteam.enums.EOSType;
import in.dragonbra.javasteam.types.ChunkData;
import org.apache.commons.lang3.SystemUtils;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.ClosedChannelException;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

/**
 * @author lngtr
 * @since 2018-02-23
 */
public class Utils {

    private static final String JAVA_RUNTIME = getSystemProperty("java.runtime.name");

    private static final Map<Boolean, EOSType> WIN_OS_MAP = new LinkedHashMap<>();

    private static final Map<Boolean, EOSType> OSX_OS_MAP = new LinkedHashMap<>();

    private static final Map<String, EOSType> LINUX_OS_MAP = new LinkedHashMap<>();

    private static final Map<String, EOSType> GENERIC_LINUX_OS_MAP = new LinkedHashMap<>();

    static {
        WIN_OS_MAP.put(SystemUtils.IS_OS_WINDOWS_95, EOSType.Win95);
        WIN_OS_MAP.put(SystemUtils.IS_OS_WINDOWS_98, EOSType.Win98);
        WIN_OS_MAP.put(SystemUtils.IS_OS_WINDOWS_ME, EOSType.WinME);
        WIN_OS_MAP.put(SystemUtils.IS_OS_WINDOWS_NT, EOSType.WinNT);
        WIN_OS_MAP.put(SystemUtils.IS_OS_WINDOWS_2000, EOSType.Win2000);
        WIN_OS_MAP.put(SystemUtils.IS_OS_WINDOWS_XP, EOSType.WinXP);
        WIN_OS_MAP.put(SystemUtils.IS_OS_WINDOWS_VISTA, EOSType.WinVista);
        WIN_OS_MAP.put(SystemUtils.IS_OS_WINDOWS_7, EOSType.Windows7);
        WIN_OS_MAP.put(SystemUtils.IS_OS_WINDOWS_8, EOSType.Windows8);
        WIN_OS_MAP.put(SystemUtils.IS_OS_WINDOWS_10, EOSType.Windows10);
        WIN_OS_MAP.put(SystemUtils.IS_OS_WINDOWS_11, EOSType.Win11);
        WIN_OS_MAP.put(SystemUtils.IS_OS_WINDOWS_2003, EOSType.Win2003);
        WIN_OS_MAP.put(SystemUtils.IS_OS_WINDOWS_2008, EOSType.Win2008);
        WIN_OS_MAP.put(SystemUtils.IS_OS_WINDOWS_2012, EOSType.Win2012);
        WIN_OS_MAP.put(checkOS("Windows Server 2016", "10.0"), EOSType.Win2016);
        WIN_OS_MAP.put(checkOS("Windows Server 2019", "10.0"), EOSType.Win2019);
        WIN_OS_MAP.put(checkOS("Windows Server 2022", "10.0"), EOSType.Win2022);

        OSX_OS_MAP.put(SystemUtils.IS_OS_MAC_OSX_TIGER, EOSType.MacOS104);
        OSX_OS_MAP.put(SystemUtils.IS_OS_MAC_OSX_LEOPARD, EOSType.MacOS105);
        OSX_OS_MAP.put(SystemUtils.IS_OS_MAC_OSX_SNOW_LEOPARD, EOSType.MacOS106);
        OSX_OS_MAP.put(SystemUtils.IS_OS_MAC_OSX_LION, EOSType.MacOS107);
        OSX_OS_MAP.put(SystemUtils.IS_OS_MAC_OSX_MOUNTAIN_LION, EOSType.MacOS108);
        OSX_OS_MAP.put(SystemUtils.IS_OS_MAC_OSX_MAVERICKS, EOSType.MacOS109);
        OSX_OS_MAP.put(SystemUtils.IS_OS_MAC_OSX_YOSEMITE, EOSType.MacOS1010);
        OSX_OS_MAP.put(SystemUtils.IS_OS_MAC_OSX_EL_CAPITAN, EOSType.MacOS1011);
        OSX_OS_MAP.put(SystemUtils.IS_OS_MAC_OSX_SIERRA, EOSType.MacOS1012);
        OSX_OS_MAP.put(SystemUtils.IS_OS_MAC_OSX_HIGH_SIERRA, EOSType.Macos1013);
        OSX_OS_MAP.put(SystemUtils.IS_OS_MAC_OSX_MOJAVE, EOSType.Macos1014);
        OSX_OS_MAP.put(SystemUtils.IS_OS_MAC_OSX_CATALINA, EOSType.Macos1015);
        OSX_OS_MAP.put(SystemUtils.IS_OS_MAC_OSX_BIG_SUR, EOSType.MacOS11);
        OSX_OS_MAP.put(SystemUtils.IS_OS_MAC_OSX_MONTEREY, EOSType.MacOS12);
        OSX_OS_MAP.put(SystemUtils.IS_OS_MAC_OSX_VENTURA, EOSType.MacOS13);
        OSX_OS_MAP.put(SystemUtils.IS_OS_MAC_OSX_SONOMA, EOSType.MacOS14);
        OSX_OS_MAP.put(checkOS("Mac OS X", "15"), EOSType.MacOS15);

        LINUX_OS_MAP.put("2.2", EOSType.Linux22);
        LINUX_OS_MAP.put("2.4", EOSType.Linux24);
        LINUX_OS_MAP.put("2.6", EOSType.Linux26);
        LINUX_OS_MAP.put("3.2", EOSType.Linux32);
        LINUX_OS_MAP.put("3.5", EOSType.Linux35);
        LINUX_OS_MAP.put("3.6", EOSType.Linux36);
        LINUX_OS_MAP.put("3.10", EOSType.Linux310);
        LINUX_OS_MAP.put("3.16", EOSType.Linux316);
        LINUX_OS_MAP.put("3.18", EOSType.Linux318);
        LINUX_OS_MAP.put("4.1", EOSType.Linux41);
        LINUX_OS_MAP.put("4.4", EOSType.Linux44);
        LINUX_OS_MAP.put("4.9", EOSType.Linux49);
        LINUX_OS_MAP.put("4.14", EOSType.Linux414);
        LINUX_OS_MAP.put("4.19", EOSType.Linux419);
        LINUX_OS_MAP.put("5.4", EOSType.Linux54);
        LINUX_OS_MAP.put("5.10", EOSType.Linux510);

        GENERIC_LINUX_OS_MAP.put("3x", EOSType.Linux3x);
        GENERIC_LINUX_OS_MAP.put("4x", EOSType.Linux4x);
        GENERIC_LINUX_OS_MAP.put("5x", EOSType.Linux5x);
        GENERIC_LINUX_OS_MAP.put("6x", EOSType.Linux6x);
        GENERIC_LINUX_OS_MAP.put("7x", EOSType.Linux7x);
    }

    // Sorted in history order by each OS release.
    public static EOSType getOSType() {
        // Windows
        if (SystemUtils.IS_OS_WINDOWS) {
            for (Map.Entry<Boolean, EOSType> winEntry : WIN_OS_MAP.entrySet()) {
                if (winEntry.getKey()) {
                    return winEntry.getValue();
                }
            }

            return EOSType.WinUnknown;
        }

        // Mac OS
        if (SystemUtils.IS_OS_MAC) {
            for (Map.Entry<Boolean, EOSType> osxEntry : OSX_OS_MAP.entrySet()) {
                if (osxEntry.getKey()) {
                    return osxEntry.getValue();
                }
            }

            return EOSType.MacOSUnknown;
        }

        // Android
        if (JAVA_RUNTIME != null && JAVA_RUNTIME.startsWith("Android")) {
            return EOSType.AndroidUnknown;
        }

        // Linux
        if (SystemUtils.IS_OS_LINUX) {
            String linuxOsVersion = getSystemProperty("os.version");

            if (linuxOsVersion == null) {
                return EOSType.LinuxUnknown;
            }

            String[] osVersion = linuxOsVersion.split("\\.");

            if (osVersion.length < 2) {
                return EOSType.LinuxUnknown;
            }

            String version = osVersion[0] + "." + osVersion[1];

            EOSType linuxVersion = LINUX_OS_MAP.get(version);
            if (linuxVersion != null) {
                // Found Major/Minor version
                return linuxVersion;
            }

            String majorVersion = osVersion[0] + "x";
            for (Map.Entry<String, EOSType> linuxEntry : GENERIC_LINUX_OS_MAP.entrySet()) {
                if (linuxEntry.getKey().equals(majorVersion)) {
                    // Found generic Linux version
                    return linuxEntry.getValue();
                }
            }

            return EOSType.LinuxUnknown;
        }

        // Unknown OS
        return EOSType.Unknown;
    }

    @SuppressWarnings("SameParameterValue")
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
        return crc32(s.getBytes());
    }

    /**
     * Convenience method for calculating the CRC2 checksum of a byte array.
     *
     * @param bytes the byte array
     * @return long value of the CRC32
     */
    public static long crc32(byte[] bytes) {
        Checksum checksum = new CRC32();
        checksum.update(bytes, 0, bytes.length);
        return checksum.getValue();
    }
}
