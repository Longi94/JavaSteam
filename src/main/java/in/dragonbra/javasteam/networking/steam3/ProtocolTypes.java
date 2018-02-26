package in.dragonbra.javasteam.networking.steam3;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.stream.Collectors;

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
        return Arrays.stream(ProtocolTypes.values()).filter(x -> (x.code & code) == x.code)
                .collect(Collectors.toCollection(() -> EnumSet.noneOf(ProtocolTypes.class)));
    }

    public static int code(EnumSet<ProtocolTypes> flags) {
        return flags.stream().mapToInt(flag -> flag.code).reduce(0, (a, b) -> a | b);
    }
}
