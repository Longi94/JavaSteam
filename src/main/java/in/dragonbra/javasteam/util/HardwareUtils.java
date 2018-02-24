package in.dragonbra.javasteam.util;

/**
 * @author lngtr
 * @since 2018-02-24
 */
public class HardwareUtils {
    public static byte[] getMachineID() {
        // the aug 25th 2015 CM update made well-formed machine MessageObjects required for logon
        // this was flipped off shortly after the update rolled out, likely due to linux steamclients running on distros without a way to build a machineid
        // so while a valid MO isn't currently (as of aug 25th) required, they could be in the future and we'll abide by The Valve Law now
        // not *shrug*
        return new byte[0];
    }
}
