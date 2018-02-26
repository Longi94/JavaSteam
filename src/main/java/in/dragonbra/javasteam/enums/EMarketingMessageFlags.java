package in.dragonbra.javasteam.enums;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.stream.Collectors;

public enum EMarketingMessageFlags {

    None(0),
    HighPriority(1),
    PlatformWindows(2),
    PlatformMac(4),
    PlatformLinux(8),

    ;

    public static final EnumSet<EMarketingMessageFlags> PlatformRestrictions = EnumSet.of(PlatformWindows, PlatformMac, PlatformLinux);

    private final int code;

    EMarketingMessageFlags(int code) {
        this.code = code;
    }

    public int code() {
        return this.code;
    }

    public static EnumSet<EMarketingMessageFlags> from(int code) {
        return Arrays.stream(EMarketingMessageFlags.values()).filter(x -> (x.code & code) == x.code)
                .collect(Collectors.toCollection(() -> EnumSet.noneOf(EMarketingMessageFlags.class)));
    }

    public static int code(EnumSet<EMarketingMessageFlags> flags) {
        return flags.stream().map(flag -> flag.code).reduce(0, (a, b) -> a | b);
    }
}
