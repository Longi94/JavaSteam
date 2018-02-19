package in.dragonbra.javasteam.enums;

import java.util.Arrays;

public enum EChatFlags {

    Locked(1),
    InvisibleToFriends(2),
    Moderated(4),
    Unjoinable(8),

    ;

    private final int code;

    EChatFlags(int code) {
        this.code = code;
    }

    public int code() {
        return this.code;
    }

    public static EChatFlags from(int code) {
        return Arrays.stream(EChatFlags.values()).filter(x -> x.code == code).findFirst().orElse(null);
    }
}
