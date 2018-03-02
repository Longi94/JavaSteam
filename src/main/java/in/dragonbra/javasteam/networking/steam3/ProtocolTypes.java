package in.dragonbra.javasteam.networking.steam3;

import java.util.EnumSet;

/**
 * @author lngtr
 * @since 2018-02-20
 */
public enum ProtocolTypes {

    TCP(1),

    UDP(1 << 1),

    WEB_SOCKET(1 << 2);

    public static final EnumSet<ProtocolTypes> ALL = EnumSet.of(TCP, UDP, WEB_SOCKET);

    private final int code;

    ProtocolTypes(int code) {
        this.code = code;
    }

    public int code() {
        return this.code;
    }

    public static EnumSet<ProtocolTypes> from(int code) {
        EnumSet<ProtocolTypes> set = EnumSet.noneOf(ProtocolTypes.class);
        for (ProtocolTypes e : ProtocolTypes.values()) {
            if (e.code == code) {
                set.add(e);
            }
        }
        return set;
    }

    public static int code(EnumSet<ProtocolTypes> flags) {
        int code = 0;
        for (ProtocolTypes flag : flags) {
            code |= flag.code;
        }
        return code;
    }
}
