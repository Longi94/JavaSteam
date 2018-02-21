package in.dragonbra.javasteam.enums;

import java.util.Arrays;

public enum EUdpPacketType {

    Invalid((byte) 0),
    ChallengeReq((byte) 1),
    Challenge((byte) 2),
    Connect((byte) 3),
    Accept((byte) 4),
    Disconnect((byte) 5),
    Data((byte) 6),
    Datagram((byte) 7),
    Max((byte) 8),

    ;

    private final byte code;

    EUdpPacketType(byte code) {
        this.code = code;
    }

    public byte code() {
        return this.code;
    }

    public static EUdpPacketType from(byte code) {
        return Arrays.stream(EUdpPacketType.values()).filter(x -> x.code == code).findFirst().orElse(null);
    }
}
