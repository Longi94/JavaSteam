package `in`.dragonbra.javasteam.util

import `in`.dragonbra.javasteam.enums.EOSType
import org.apache.commons.lang3.SystemUtils
import java.util.zip.CRC32

/**
 * @author lngtr
 * @since 2018-02-23
 */
object Utils {

    private val javaRuntime: String? = getSystemProperty("java.runtime.name")

    private val winOsTypes: List<Pair<Boolean, EOSType>> = listOf(
        SystemUtils.IS_OS_WINDOWS_95 to EOSType.Win95,
        SystemUtils.IS_OS_WINDOWS_98 to EOSType.Win98,
        SystemUtils.IS_OS_WINDOWS_ME to EOSType.WinME,
        SystemUtils.IS_OS_WINDOWS_NT to EOSType.WinNT,
        SystemUtils.IS_OS_WINDOWS_2000 to EOSType.Win2000,
        SystemUtils.IS_OS_WINDOWS_XP to EOSType.WinXP,
        SystemUtils.IS_OS_WINDOWS_VISTA to EOSType.WinVista,
        SystemUtils.IS_OS_WINDOWS_7 to EOSType.Windows7,
        SystemUtils.IS_OS_WINDOWS_8 to EOSType.Windows8,
        SystemUtils.IS_OS_WINDOWS_10 to EOSType.Windows10,
        SystemUtils.IS_OS_WINDOWS_11 to EOSType.Win11,
        SystemUtils.IS_OS_WINDOWS_2003 to EOSType.Win2003,
        SystemUtils.IS_OS_WINDOWS_2008 to EOSType.Win2008,
        SystemUtils.IS_OS_WINDOWS_2012 to EOSType.Win2012,
        checkOS("Windows Server 2016", "10.0") to EOSType.Win2016,
        checkOS("Windows Server 2019", "10.0") to EOSType.Win2019,
        checkOS("Windows Server 2022", "10.0") to EOSType.Win2022,
    )

    // Big Sur point releases (11.1 and later) have their own EOSType
    private val isMacOS111: Boolean = checkOS("Mac OS X", "11.") && !checkOS("Mac OS X", "11.0")

    private val osxOsTypes: List<Pair<Boolean, EOSType>> = listOf(
        SystemUtils.IS_OS_MAC_OSX_TIGER to EOSType.MacOS104,
        SystemUtils.IS_OS_MAC_OSX_LEOPARD to EOSType.MacOS105,
        SystemUtils.IS_OS_MAC_OSX_SNOW_LEOPARD to EOSType.MacOS106,
        SystemUtils.IS_OS_MAC_OSX_LION to EOSType.MacOS107,
        SystemUtils.IS_OS_MAC_OSX_MOUNTAIN_LION to EOSType.MacOS108,
        SystemUtils.IS_OS_MAC_OSX_MAVERICKS to EOSType.MacOS109,
        SystemUtils.IS_OS_MAC_OSX_YOSEMITE to EOSType.MacOS1010,
        SystemUtils.IS_OS_MAC_OSX_EL_CAPITAN to EOSType.MacOS1011,
        SystemUtils.IS_OS_MAC_OSX_SIERRA to EOSType.MacOS1012,
        SystemUtils.IS_OS_MAC_OSX_HIGH_SIERRA to EOSType.Macos1013,
        SystemUtils.IS_OS_MAC_OSX_MOJAVE to EOSType.Macos1014,
        SystemUtils.IS_OS_MAC_OSX_CATALINA to EOSType.Macos1015,
        // 10.16 and later are what macOS reports to apps linked against an older SDK
        checkOS("Mac OS X", "10.16") to EOSType.MacOS1016,
        checkOS("Mac OS X", "10.17") to EOSType.MacOS1017,
        checkOS("Mac OS X", "10.18") to EOSType.MacOS1018,
        checkOS("Mac OS X", "10.19") to EOSType.MacOS1019,
        checkOS("Mac OS X", "10.20") to EOSType.MacOS1020,
        checkOS("Mac OS X", "10.21") to EOSType.MacOS1021,
        checkOS("Mac OS X", "10.22") to EOSType.MacOS1022,
        checkOS("Mac OS X", "10.23") to EOSType.MacOS1023,
        (SystemUtils.IS_OS_MAC_OSX_BIG_SUR && !isMacOS111) to EOSType.MacOS11,
        isMacOS111 to EOSType.MacOS111,
        SystemUtils.IS_OS_MAC_OSX_MONTEREY to EOSType.MacOS12,
        SystemUtils.IS_OS_MAC_OSX_VENTURA to EOSType.MacOS13,
        SystemUtils.IS_OS_MAC_OSX_SONOMA to EOSType.MacOS14,
        SystemUtils.IS_OS_MAC_OSX_SEQUOIA to EOSType.MacOS15,
        checkOS("Mac OS X", "26") to EOSType.MacOS26, // Tahoe
        checkOS("Mac OS X", "27") to EOSType.MacOS27, // Golden Gate
        checkOS("Mac OS X", "28") to EOSType.MacOS28, // Codename: Poppy
    )

    private val linuxOsMap: Map<String, EOSType> = linkedMapOf(
        "2.2" to EOSType.Linux22,
        "2.4" to EOSType.Linux24,
        "2.6" to EOSType.Linux26,
        "3.2" to EOSType.Linux32,
        "3.5" to EOSType.Linux35,
        "3.6" to EOSType.Linux36,
        "3.10" to EOSType.Linux310,
        "3.16" to EOSType.Linux316,
        "3.18" to EOSType.Linux318,
        "4.1" to EOSType.Linux41,
        "4.4" to EOSType.Linux44,
        "4.9" to EOSType.Linux49,
        "4.14" to EOSType.Linux414,
        "4.19" to EOSType.Linux419,
        "5.4" to EOSType.Linux54,
        "5.10" to EOSType.Linux510,
    )

    private val genericLinuxOsMap: Map<String, EOSType> = linkedMapOf(
        "3x" to EOSType.Linux3x,
        "4x" to EOSType.Linux4x,
        "5x" to EOSType.Linux5x,
        "6x" to EOSType.Linux6x,
        "7x" to EOSType.Linux7x,
    )

    // Sorted in history order by each OS release.
    // Android also reports itself as Linux, so it has to be checked before Linux.
    @JvmStatic
    fun getOSType(): EOSType = when {
        SystemUtils.IS_OS_WINDOWS -> winOsTypes.firstMatch() ?: EOSType.WinUnknown
        SystemUtils.IS_OS_MAC -> osxOsTypes.firstMatch() ?: EOSType.MacOSUnknown
        javaRuntime?.startsWith("Android") == true -> EOSType.AndroidUnknown
        SystemUtils.IS_OS_LINUX -> getLinuxOSType()
        else -> EOSType.Unknown
    }

    private fun getLinuxOSType(): EOSType {
        val osVersion = getSystemProperty("os.version")?.split(".") ?: return EOSType.LinuxUnknown
        if (osVersion.size < 2) {
            return EOSType.LinuxUnknown
        }

        // Exact major.minor version first, then the generic major version
        return linuxOsMap["${osVersion[0]}.${osVersion[1]}"]
            ?: genericLinuxOsMap["${osVersion[0]}x"]
            ?: EOSType.LinuxUnknown
    }

    private fun List<Pair<Boolean, EOSType>>.firstMatch(): EOSType? = firstOrNull { it.first }?.second

    private fun checkOS(namePrefix: String, versionPrefix: String): Boolean =
        SystemUtils.OS_NAME.startsWith(namePrefix) && SystemUtils.OS_VERSION.startsWith(versionPrefix)

    private fun getSystemProperty(property: String): String? = try {
        System.getProperty(property)
    } catch (_: SecurityException) {
        // we are not allowed to look at this property
        null
    }

    /**
     * Convenience method for calculating the CRC32 checksum of a string.
     * @param s the string
     * @return long value of the CRC32
     */
    @JvmStatic
    fun crc32(s: String): Long = crc32(s.toByteArray())

    /**
     * Convenience method for calculating the CRC32 checksum of a byte array.
     * @param bytes the byte array
     * @return long value of the CRC32
     */
    @JvmStatic
    fun crc32(bytes: ByteArray): Long = crc32(bytes, 0, bytes.size)

    /**
     * Convenience method for calculating the CRC32 checksum of a byte array with offset and length.
     * @param bytes  the byte array
     * @param offset the offset to start from
     * @param length the number of bytes to checksum
     * @return long value of the CRC32
     */
    @JvmStatic
    fun crc32(bytes: ByteArray, offset: Int, length: Int): Long {
        val checksum = CRC32()
        checksum.update(bytes, offset, length)
        return checksum.value
    }
}
