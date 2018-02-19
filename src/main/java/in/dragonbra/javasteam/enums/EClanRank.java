package in.dragonbra.javasteam.enums;

import java.util.Arrays;

public enum EClanRank {

    None(0),
    Owner(1),
    Officer(2),
    Member(3),
    Moderator(4),

    ;

    private final int code;

    EClanRank(int code) {
        this.code = code;
    }

    public int code() {
        return this.code;
    }

    public static EClanRank from(int code) {
        return Arrays.stream(EClanRank.values()).filter(x -> x.code == code).findFirst().orElse(null);
    }
}
