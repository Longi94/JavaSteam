package in.dragonbra.javasteam.enums;

import java.util.Arrays;

public enum EPublishedFileVisibility {

    Public(0),
    FriendsOnly(1),
    Private(2),

    ;

    private final int code;

    EPublishedFileVisibility(int code) {
        this.code = code;
    }

    public int code() {
        return this.code;
    }

    public static EPublishedFileVisibility from(int code) {
        return Arrays.stream(EPublishedFileVisibility.values()).filter(x -> x.code == code).findFirst().orElse(null);
    }
}
