package in.dragonbra.javasteam.util;

import org.apache.commons.lang3.SystemUtils;

import java.io.*;
import java.lang.reflect.Method;
import java.util.Scanner;

/**
 * @author lngtr
 * @since 2018-02-24
 */
public class HardwareUtils {

    // Everything taken from here
    // https://stackoverflow.com/questions/1986732/how-to-get-a-unique-computer-identifier-in-java-like-disk-id-or-motherboard-id
    private static String SERIAL_NUMBER;
    private static String MACHINE_NAME;

    public static byte[] getMachineID() {
        // the aug 25th 2015 CM update made well-formed machine MessageObjects required for logon
        // this was flipped off shortly after the update rolled out, likely due to linux steamclients running on distros without a way to build a machineid
        // so while a valid MO isn't currently (as of aug 25th) required, they could be in the future and we'll abide by The Valve Law now

        if (SERIAL_NUMBER != null) {
            return SERIAL_NUMBER.getBytes();
        }

        if (SystemUtils.IS_OS_WINDOWS) {
            SERIAL_NUMBER = getSerialNumberWin();
        }
        if (SystemUtils.IS_OS_MAC) {
            SERIAL_NUMBER = getSerialNumberMac();
        }
        if (SystemUtils.IS_OS_LINUX) {
            SERIAL_NUMBER = getSerialNumberUnix();
        }

        // if SERIAL_NUMBER still was null
        if (SERIAL_NUMBER == null) {
            SERIAL_NUMBER = "JavaSteam-SerialNumber";
        }

        return SERIAL_NUMBER.getBytes();
    }

    private static String getSerialNumberWin() {
        String sn = null;

        Runtime runtime = Runtime.getRuntime();
        Process process;

        try {
            process = runtime.exec(new String[]{"wmic", "bios", "get", "serialnumber"});
        } catch (IOException e) {
            return null;
        }

        OutputStream os = process.getOutputStream();

        try {
            os.close();
        } catch (IOException ignored) {
        }

        try (Scanner sc = new Scanner(process.getInputStream())) {
            while (sc.hasNext()) {
                String next = sc.next();
                if ("SerialNumber".equals(next)) {
                    sn = sc.next().trim();
                    break;
                }
            }
        }

        return sn;
    }

    private static String getSerialNumberMac() {
        String sn = null;

        Runtime runtime = Runtime.getRuntime();
        Process process;

        try {
            process = runtime.exec(new String[]{"/usr/sbin/system_profiler", "SPHardwareDataType"});
        } catch (IOException e) {
            return null;
        }

        OutputStream os = process.getOutputStream();

        try {
            os.close();
        } catch (IOException ignored) {
        }

        String line;
        String marker = "Serial Number";
        try (BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            while ((line = br.readLine()) != null) {
                if (line.contains(marker)) {
                    sn = line.split(":")[1].trim();
                    break;
                }
            }
        } catch (IOException e) {
            return null;
        }

        return sn;
    }

    private static String getSerialNumberUnix() {
        String sn = readDmidecode();

        if (sn == null) {
            sn = readLshal();
        }

        return sn;
    }

    private static BufferedReader read(String command) {

        Runtime runtime = Runtime.getRuntime();
        Process process;
        try {
            process = runtime.exec(command.split(" "));
        } catch (IOException e) {
            return null;
        }

        OutputStream os = process.getOutputStream();

        try {
            os.close();
        } catch (IOException ignored) {
        }

        return new BufferedReader(new InputStreamReader(process.getInputStream()));
    }

    private static String readDmidecode() {

        String sn = null;

        String line;
        String marker = "Serial Number:";

        try (BufferedReader br = read("dmidecode -t system")) {
            if (br == null) {
                return null;
            }

            while ((line = br.readLine()) != null) {
                if (line.contains(marker)) {
                    sn = line.split(marker)[1].trim();
                    break;
                }
            }
        } catch (IOException e) {
            return null;
        }

        return sn;
    }

    private static String readLshal() {
        String sn = null;

        String line;
        String marker = "system.hardware.serial =";

        try (BufferedReader br = read("lshal")) {
            if (br == null) {
                return null;
            }
            while ((line = br.readLine()) != null) {
                if (line.contains(marker)) {
                    //noinspection RegExpRedundantEscape
                    sn = line.split(marker)[1].replaceAll("\\(string\\)|(\\')", "").trim();
                    break;
                }
            }
        } catch (IOException e) {
            return null;
        }

        return sn;
    }

    public static String getMachineName() {
        if (MACHINE_NAME != null) {
            return MACHINE_NAME;
        }

        if (SystemUtils.IS_OS_ANDROID) {
            MACHINE_NAME = getAndroidDeviceName();
        } else {
            MACHINE_NAME = getDeviceName();
        }

        if (MACHINE_NAME == null || MACHINE_NAME.isEmpty()) {
            MACHINE_NAME = "Unknown";
        }

        return MACHINE_NAME;
    }

    private static String getDeviceName() {
        try {
            Process process = Runtime.getRuntime().exec("hostname");
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                return reader.readLine().trim();
            }
        } catch (IOException e) {
            return null;
        }
    }

    private static String getAndroidDeviceName() {
        String manufacturer = getAndroidSystemProperty("ro.product.manufacturer");
        String model = getAndroidSystemProperty("ro.product.model");

        if (manufacturer == null || model == null) {
            return "Android Device";
        }

        if (model.startsWith(manufacturer)) {
            return model;
        }
        return manufacturer + " " + model;
    }

    private static String getAndroidSystemProperty(String key) {
        try {
            Class<?> systemProperties = Class.forName("android.os.SystemProperties");
            Method get = systemProperties.getMethod("get", String.class);
            return (String) get.invoke(null, key);
        } catch (Exception e) {
            return null;
        }
    }
}
