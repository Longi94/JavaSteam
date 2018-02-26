package in.dragonbra.javasteam.enums;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.stream.Collectors;

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

    public int code() {
        return this.code;
    }

    public static EnumSet<EPersonaStateFlag> from(int code) {
        return Arrays.stream(EPersonaStateFlag.values()).filter(x -> (x.code & code) == x.code)
                .collect(Collectors.toCollection(() -> EnumSet.noneOf(EPersonaStateFlag.class)));
    }

    public static int code(EnumSet<EPersonaStateFlag> flags) {
        return flags.stream().map(flag -> flag.code).reduce(0, (a, b) -> a | b);
    }
}
