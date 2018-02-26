package in.dragonbra.javasteam.enums;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.stream.Collectors;

public enum ERemoteStoragePlatform {

    None(0),
    Windows(1),
    OSX(2),
    PS3(4),
    Linux(8),
    Reserved2(16),
    All(-1),

    ;

    private final int code;

    ERemoteStoragePlatform(int code) {
        this.code = code;
    }

    public int code() {
        return this.code;
    }

    public static EnumSet<ERemoteStoragePlatform> from(int code) {
        return Arrays.stream(ERemoteStoragePlatform.values()).filter(x -> (x.code & code) == x.code)
                .collect(Collectors.toCollection(() -> EnumSet.noneOf(ERemoteStoragePlatform.class)));
    }

    public static int code(EnumSet<ERemoteStoragePlatform> flags) {
        return flags.stream().map(flag -> flag.code).reduce(0, (a, b) -> a | b);
    }
}
