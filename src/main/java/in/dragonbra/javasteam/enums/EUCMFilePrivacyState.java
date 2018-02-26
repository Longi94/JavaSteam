package in.dragonbra.javasteam.enums;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.stream.Collectors;

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

    public int code() {
        return this.code;
    }

    public static EnumSet<EUCMFilePrivacyState> from(int code) {
        return Arrays.stream(EUCMFilePrivacyState.values()).filter(x -> (x.code & code) == x.code)
                .collect(Collectors.toCollection(() -> EnumSet.noneOf(EUCMFilePrivacyState.class)));
    }

    public static int code(EnumSet<EUCMFilePrivacyState> flags) {
        return flags.stream().map(flag -> flag.code).reduce(0, (a, b) -> a | b);
    }
}
