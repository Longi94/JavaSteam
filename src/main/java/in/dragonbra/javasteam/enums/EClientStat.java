package in.dragonbra.javasteam.enums;

import java.util.Arrays;

public enum EClientStat {

    P2PConnectionsUDP(0),
    P2PConnectionsRelay(1),
    P2PGameConnections(2),
    P2PVoiceConnections(3),
    BytesDownloaded(4),
    Max(5),

    ;

    private final int code;

    EClientStat(int code) {
        this.code = code;
    }

    public int getCode() {
        return this.code;
    }

    public EClientStat from(int code) {
        return Arrays.stream(EClientStat.values()).filter(x -> x.code == code).findFirst().orElse(null);
    }
}
