package in.dragonbra.javasteam.enums;

import java.util.Arrays;

public enum EActivationCodeClass {

    WonCDKey(0L),
    ValveCDKey(1L),
    Doom3CDKey(2L),
    DBLookup(3L),
    Steam2010Key(4L),
    Max(5L),
    Test(2147483647L),
    Invalid(4294967295L),

    ;

    private final long code;

    EActivationCodeClass(long code) {
        this.code = code;
    }

    public long getCode() {
        return this.code;
    }

    public EActivationCodeClass from(long code) {
        return Arrays.stream(EActivationCodeClass.values()).filter(x -> x.code == code).findFirst().orElse(null);
    }
}
