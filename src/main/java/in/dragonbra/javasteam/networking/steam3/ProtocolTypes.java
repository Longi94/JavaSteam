package in.dragonbra.javasteam.networking.steam3;

import java.util.Arrays;

/**
 * @author lngtr
 * @since 2018-02-20
 */
public enum ProtocolTypes {

    TCP(1),

    UDP(1 << 1),

    WEB_SOCKET(1 << 2),

    TCP_UDP(TCP.code | UDP.code),

    ALL(TCP.code | UDP.code | WEB_SOCKET.code);

    private final int code;

    ProtocolTypes(int code) {
        this.code = code;
    }

    public int code() {
        return this.code;
    }

    public static ProtocolTypes from(int code) {
        return Arrays.stream(ProtocolTypes.values()).filter(x -> x.code == code).findFirst().orElse(null);
    }
}
