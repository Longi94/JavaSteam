package in.dragonbra.javasteam.enums;

import java.util.Arrays;

public enum EPersonaStateFlag {

    HasRichPresence(1),
    InJoinableGame(2),
    ClientTypeWeb(256),
    ClientTypeMobile(512),
    ClientTypeTenfoot(1024),
    ClientTypeVR(2048),
    LaunchTypeGamepad(4096),

    ;

    private final int code;

    EPersonaStateFlag(int code) {
        this.code = code;
    }

    public int getCode() {
        return this.code;
    }

    public EPersonaStateFlag from(int code) {
        return Arrays.stream(EPersonaStateFlag.values()).filter(x -> x.code == code).findFirst().orElse(null);
    }
}
