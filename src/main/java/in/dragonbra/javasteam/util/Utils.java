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

    // Sorted in history order by each OS release.
    public static EOSType getOSType() {
        // Windows
        if (SystemUtils.IS_OS_WINDOWS) {
            // Windows 9x
            if (SystemUtils.IS_OS_WINDOWS_95) {
                return EOSType.Win95;
            }
            if (SystemUtils.IS_OS_WINDOWS_98) {
                return EOSType.Win98;
            }
            if (SystemUtils.IS_OS_WINDOWS_ME) {
                return EOSType.WinME;
            }
            // Windows NT
            if (SystemUtils.IS_OS_WINDOWS_NT) {
                return EOSType.WinNT;
            }
            if (SystemUtils.IS_OS_WINDOWS_2000) {
                return EOSType.Win2000;
            }
            if (SystemUtils.IS_OS_WINDOWS_XP) {
                return EOSType.WinXP;
            }
            if (SystemUtils.IS_OS_WINDOWS_VISTA) {
                return EOSType.WinVista;
            }
            if (SystemUtils.IS_OS_WINDOWS_7) {
                return EOSType.Windows7;
            }
            if (SystemUtils.IS_OS_WINDOWS_8) {
                return EOSType.Windows8;
            }
            if (SystemUtils.IS_OS_WINDOWS_10) {
                return EOSType.Windows10;
            }
            if (checkOS("Windows 11", "10.0")) {
                return EOSType.Win11;
            }
            // Windows Server
            if (SystemUtils.IS_OS_WINDOWS_2003) {
                return EOSType.Win2003;
            }
            if (SystemUtils.IS_OS_WINDOWS_2008) {
                return EOSType.Win2008;
            }
            if (SystemUtils.IS_OS_WINDOWS_2012) {
                return EOSType.Win2012;
            }
            if (checkOS("Windows Server 2016", "10.0")) {
                return EOSType.Win2016;
            }
            if (checkOS("Windows Server 2019", "10.0")) {
                return EOSType.Win2019;
            }
            if (checkOS("Windows Server 2022", "10.0")) {
                return EOSType.Win2022;
            }
            // Windows Unknown
            return EOSType.WinUnknown;
        }
        // Mac OS
        if (SystemUtils.IS_OS_MAC) {
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
            if (SystemUtils.IS_OS_MAC_OSX_SIERRA) {
                return EOSType.MacOS1012;
            }
            if (SystemUtils.IS_OS_MAC_OSX_HIGH_SIERRA) {
                return EOSType.Macos1013;
            }
            if (SystemUtils.IS_OS_MAC_OSX_MOJAVE) {
                return EOSType.Macos1014;
            }
            if (SystemUtils.IS_OS_MAC_OSX_CATALINA) {
                return EOSType.Macos1015;
            }
            if (SystemUtils.IS_OS_MAC_OSX_BIG_SUR) {
                return EOSType.MacOS11;
            }
            // TODO: Apache Commons missing: macOS Monterey
            // TODO: Apache Commons missing: macOS Ventura
            // Mac OS Unknown
            return EOSType.MacOSUnknown;
        }
        // Android
        if (JAVA_RUNTIME != null && JAVA_RUNTIME.startsWith("Android")) {
            return EOSType.AndroidUnknown;
        }
        // Linux
        if (SystemUtils.IS_OS_LINUX) {
            String linuxOsVersion = getSystemProperty("os.version");
            String[] osVersion;

            if (linuxOsVersion == null) {
                return EOSType.Unknown;
            }

            osVersion = linuxOsVersion.split("\\.");

            // Major
            switch (osVersion[0]) {
                case "2":
                    // Minor
                    switch (osVersion[1]) {
                        case "2":
                            return EOSType.Linux22;
                        case "4":
                            return EOSType.Linux24;
                        case "6":
                            return EOSType.Linux26;
                        default:
                            return EOSType.LinuxUnknown;
                    }
                case "3":
                    // Minor
                    switch (osVersion[1]) {
                        case "2":
                            return EOSType.Linux32;
                        case "5":
                            return EOSType.Linux35;
                        case "6":
                            return EOSType.Linux36;
                        case "10":
                            return EOSType.Linux310;
                        case "16":
                            return EOSType.Linux316;
                        case "18":
                            return EOSType.Linux318;
                        default:
                            return EOSType.Linux3x;
                    }
                case "4":
                    // Minor
                    switch (osVersion[1]) {
                        case "1":
                            return EOSType.Linux41;
                        case "4":
                            return EOSType.Linux44;
                        case "9":
                            return EOSType.Linux49;
                        case "14":
                            return EOSType.Linux414;
                        case "19":
                            return EOSType.Linux419;
                        default:
                            return EOSType.Linux4x;
                    }
                case "5":
                    // Minor
                    switch (osVersion[1]) {
                        case "4":
                            return EOSType.Linux54;
                        case "10":
                            return EOSType.Linux510;
                        default:
                            return EOSType.Linux5x;
                    }
                case "6":
                    return EOSType.Linux6x;
                case "7":
                    return EOSType.Linux7x;
                default:
                    return EOSType.LinuxUnknown;
            }
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
        Checksum checksum = new CRC32();
        byte[] bytes = s.getBytes();
        checksum.update(bytes, 0, bytes.length);
        return checksum.getValue();
    }
}
