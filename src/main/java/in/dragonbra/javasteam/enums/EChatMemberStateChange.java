package in.dragonbra.javasteam.enums;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.stream.Collectors;

public enum EChatMemberStateChange {

    Entered(0x01),
    Left(0x02),
    Disconnected(0x04),
    Kicked(0x08),
    Banned(0x10),
    VoiceSpeaking(0x1000),
    VoiceDoneSpeaking(0x2000),

    ;

    private final int code;

    EChatMemberStateChange(int code) {
        this.code = code;
    }

    public int code() {
        return this.code;
    }

    public static EnumSet<EChatMemberStateChange> from(int code) {
        return Arrays.stream(EChatMemberStateChange.values()).filter(x -> (x.code & code) == x.code)
                .collect(Collectors.toCollection(() -> EnumSet.noneOf(EChatMemberStateChange.class)));
    }

    public static int code(EnumSet<EChatMemberStateChange> flags) {
        return flags.stream().map(flag -> flag.code).reduce(0, (a, b) -> a | b);
    }
}
