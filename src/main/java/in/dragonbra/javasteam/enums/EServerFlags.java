package in.dragonbra.javasteam.enums;

import java.util.Arrays;

public enum EServerFlags {

    None(0),
    Active(1),
    Secure(2),
    Dedicated(4),
    Linux(8),
    Passworded(16),
    Private(32),

    ;

    private final int code;

    EServerFlags(int code) {
        this.code = code;
    }

    public int code() {
        return this.code;
    }

    public static EServerFlags from(int code) {
        return Arrays.stream(EServerFlags.values()).filter(x -> x.code == code).findFirst().orElse(null);
    }
}
