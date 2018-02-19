package in.dragonbra.javasteam.enums;

import java.util.Arrays;

public enum ELeaderboardUploadScoreMethod {

    None(0),
    KeepBest(1),
    ForceUpdate(2),

    ;

    private final int code;

    ELeaderboardUploadScoreMethod(int code) {
        this.code = code;
    }

    public int code() {
        return this.code;
    }

    public static ELeaderboardUploadScoreMethod from(int code) {
        return Arrays.stream(ELeaderboardUploadScoreMethod.values()).filter(x -> x.code == code).findFirst().orElse(null);
    }
}
