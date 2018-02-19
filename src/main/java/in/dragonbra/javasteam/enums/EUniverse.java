package in.dragonbra.javasteam.enums;

import java.util.Arrays;

public enum EUniverse {

    Invalid(0),
    Public(1),
    Beta(2),
    Internal(3),
    Dev(4),
    Max(5),

    ;

    private final int code;

    EUniverse(int code) {
        this.code = code;
    }

    public int getCode() {
        return this.code;
    }

    public EUniverse from(int code) {
        return Arrays.stream(EUniverse.values()).filter(x -> x.code == code).findFirst().orElse(null);
    }
}
