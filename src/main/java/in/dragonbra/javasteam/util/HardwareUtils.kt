package `in`.dragonbra.javasteam.util

import org.apache.commons.lang3.SystemUtils
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.StringReader
import java.net.InetAddress
import java.util.Scanner

/**
 * @author lngtr
 * @since 2018-02-24
 */
// https://stackoverflow.com/questions/1986732/how-to-get-a-unique-computer-identifier-in-java-like-disk-id-or-motherboard-id
object HardwareUtils {

    private val serialNumber: String by lazy {
        when {
            SystemUtils.IS_OS_WINDOWS -> getSerialNumberWin()
            SystemUtils.IS_OS_MAC -> getSerialNumberMac()
            SystemUtils.IS_OS_LINUX -> getSerialNumberUnix()
            else -> null
        } ?: "JavaSteam-SerialNumber"
    }

    private val resolvedMachineName: String by lazy {
        val name = if (SystemUtils.IS_OS_ANDROID) getAndroidDeviceName() else getDeviceName()
        name.takeUnless { it.isNullOrBlank() } ?: "Unknown"
    }

    // the aug 25th 2015 CM update made well-formed machine MessageObjects required for logon
    // this was flipped off shortly after the update rolled out, likely due to linux steamclients running on distros without a way to build a machineid
    // so while a valid MO isn't currently (as of aug 25th) required, they could be in the future and we'll abide by The Valve Law now
    @JvmStatic
    fun getMachineID(): ByteArray = serialNumber.toByteArray()

    @JvmStatic
    @JvmOverloads
    fun getMachineName(addTag: Boolean = false): String =
        if (addTag || resolvedMachineName.contains("Unknown")) "$resolvedMachineName (JavaSteam)" else resolvedMachineName

    private fun getSerialNumberWin(): String? = runCatching {
        val process = Runtime.getRuntime().exec(arrayOf("wmic", "bios", "get", "serialnumber"))
        runCatching { process.outputStream.close() }
        Scanner(process.inputStream).use { sc ->
            while (sc.hasNext()) {
                if (sc.next() == "SerialNumber") return@runCatching sc.next().trim()
            }
            null
        }
    }.getOrNull()

    private fun getSerialNumberMac(): String? = runCatching {
        val process = Runtime.getRuntime().exec(arrayOf("/usr/sbin/system_profiler", "SPHardwareDataType"))
        runCatching { process.outputStream.close() }
        BufferedReader(InputStreamReader(process.inputStream)).use { br ->
            br.lineSequence()
                .firstOrNull { it.contains("Serial Number") }
                ?.substringAfter(":")
                ?.trim()
        }
    }.getOrNull()

    private fun getSerialNumberUnix(): String? = readDmidecode() ?: readLshal()

    private fun read(command: String): BufferedReader? {
        val process = runCatching {
            Runtime.getRuntime().exec(command.split(" ").toTypedArray())
        }.getOrNull() ?: return null

        runCatching { process.outputStream.close() }

        return runCatching {
            BufferedReader(InputStreamReader(process.inputStream)).use { br ->
                BufferedReader(StringReader(br.readText()))
            }
        }.also {
            process.destroy()
        }.getOrNull()
    }

    private fun readDmidecode(): String? =
        read("dmidecode -t system")?.use { br ->
            br.lineSequence()
                .firstOrNull { it.contains("Serial Number:") }
                ?.substringAfter("Serial Number:")
                ?.trim()
        }

    private fun readLshal(): String? =
        read("lshal")?.use { br ->
            br.lineSequence()
                .firstOrNull { it.contains("system.hardware.serial =") }
                ?.substringAfter("system.hardware.serial =")
                ?.replace("(string)", "")
                ?.replace("'", "")
                ?.trim()
        }

    private fun getDeviceName(): String? {
        val hostname = SystemUtils.getHostName()
        if (!hostname.isNullOrBlank()) return hostname
        return runCatching { InetAddress.getLocalHost().hostName }.getOrNull()
    }

    private fun getAndroidDeviceName(): String? {
        val manufacturer = getAndroidSystemProperty("ro.product.manufacturer")
        val model = getAndroidSystemProperty("ro.product.model")
        if (manufacturer == null || model == null) return "Android Device"
        return if (model.startsWith(manufacturer)) model else "$manufacturer $model"
    }

    private fun getAndroidSystemProperty(key: String): String? = runCatching {
        val systemProperties = Class.forName("android.os.SystemProperties")
        val get = systemProperties.getMethod("get", String::class.java)
        get.invoke(null, key) as? String
    }.getOrNull()
}
