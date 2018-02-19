package in.dragonbra.javasteam.enums;

import java.util.Arrays;

public enum EUCMFilePrivacyState {

    Invalid(-1),
    Private(2),
    FriendsOnly(4),
    Public(8),
    All(Public.code | FriendsOnly.code | Private.code),

    ;

    private final int code;

    EUCMFilePrivacyState(int code) {
        this.code = code;
    }

    public int getCode() {
        return this.code;
    }

    public EUCMFilePrivacyState from(int code) {
        return Arrays.stream(EUCMFilePrivacyState.values()).filter(x -> x.code == code).findFirst().orElse(null);
    }
}
