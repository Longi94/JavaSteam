package in.dragonbra.javasteam.enums;

import java.util.Arrays;

public enum EMarketingMessageFlags {

    None(0),
    HighPriority(1),
    PlatformWindows(2),
    PlatformMac(4),
    PlatformLinux(8),
    PlatformRestrictions(PlatformWindows.code | PlatformMac.code | PlatformLinux.code),

    ;

    private final int code;

    EMarketingMessageFlags(int code) {
        this.code = code;
    }

    public int code() {
        return this.code;
    }

    public static EMarketingMessageFlags from(int code) {
        return Arrays.stream(EMarketingMessageFlags.values()).filter(x -> x.code == code).findFirst().orElse(null);
    }
}
