package in.dragonbra.javasteam.enums;

import java.util.Arrays;

public enum EActivationCodeClass {

    WonCDKey(0),
    ValveCDKey(1),
    Doom3CDKey(2),
    DBLookup(3),
    Steam2010Key(4),
    Max(5),
    Test(2147483647),
    Invalid(-1),

    ;

    private final int code;

    EActivationCodeClass(int code) {
        this.code = code;
    }

    public int code() {
        return this.code;
    }

    public static EActivationCodeClass from(int code) {
        return Arrays.stream(EActivationCodeClass.values()).filter(x -> x.code == code).findFirst().orElse(null);
    }
}
