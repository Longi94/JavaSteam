package in.dragonbra.javasteam.enums;

import java.util.Arrays;

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

    public static EChatMemberStateChange from(int code) {
        return Arrays.stream(EChatMemberStateChange.values()).filter(x -> x.code == code).findFirst().orElse(null);
    }
}
