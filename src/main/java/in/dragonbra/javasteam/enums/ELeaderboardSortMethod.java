package in.dragonbra.javasteam.enums;

import java.util.Arrays;

public enum ELeaderboardSortMethod {

    None(0),
    Ascending(1),
    Descending(2),

    ;

    private final int code;

    ELeaderboardSortMethod(int code) {
        this.code = code;
    }

    public int getCode() {
        return this.code;
    }

    public ELeaderboardSortMethod from(int code) {
        return Arrays.stream(ELeaderboardSortMethod.values()).filter(x -> x.code == code).findFirst().orElse(null);
    }
}
