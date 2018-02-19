package in.dragonbra.javasteam.enums;

import java.util.Arrays;

public enum EPlatformType {

    Unknown(0),
    Win32(1),
    Win64(2),
    Linux64(3),
    OSX(4),
    PS3(5),
    Linux32(6),
    Max(6),

    ;

    private final int code;

    EPlatformType(int code) {
        this.code = code;
    }

    public int getCode() {
        return this.code;
    }

    public EPlatformType from(int code) {
        return Arrays.stream(EPlatformType.values()).filter(x -> x.code == code).findFirst().orElse(null);
    }
}
