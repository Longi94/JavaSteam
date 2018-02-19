package in.dragonbra.javasteam.enums;

import java.util.Arrays;

public enum ELeaderboardDisplayType {

    None(0),
    Numeric(1),
    TimeSeconds(2),
    TimeMilliSeconds(3),

    ;

    private final int code;

    ELeaderboardDisplayType(int code) {
        this.code = code;
    }

    public int code() {
        return this.code;
    }

    public static ELeaderboardDisplayType from(int code) {
        return Arrays.stream(ELeaderboardDisplayType.values()).filter(x -> x.code == code).findFirst().orElse(null);
    }
}
