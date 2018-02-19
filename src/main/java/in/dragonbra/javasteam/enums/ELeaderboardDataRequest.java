package in.dragonbra.javasteam.enums;

import java.util.Arrays;

public enum ELeaderboardDataRequest {

    Global(0),
    GlobalAroundUser(1),
    Friends(2),
    Users(3),

    ;

    private final int code;

    ELeaderboardDataRequest(int code) {
        this.code = code;
    }

    public int code() {
        return this.code;
    }

    public static ELeaderboardDataRequest from(int code) {
        return Arrays.stream(ELeaderboardDataRequest.values()).filter(x -> x.code == code).findFirst().orElse(null);
    }
}
