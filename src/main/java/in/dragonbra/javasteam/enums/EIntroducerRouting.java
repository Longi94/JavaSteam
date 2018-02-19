package in.dragonbra.javasteam.enums;

import java.util.Arrays;

public enum EIntroducerRouting {

    P2PVoiceChat(1),
    P2PNetworking(2),

    ;

    private final int code;

    EIntroducerRouting(int code) {
        this.code = code;
    }

    public int code() {
        return this.code;
    }

    public static EIntroducerRouting from(int code) {
        return Arrays.stream(EIntroducerRouting.values()).filter(x -> x.code == code).findFirst().orElse(null);
    }
}
