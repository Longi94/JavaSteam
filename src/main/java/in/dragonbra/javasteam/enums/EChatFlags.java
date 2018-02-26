package in.dragonbra.javasteam.enums;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.stream.Collectors;

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

    public static EnumSet<EChatFlags> from(int code) {
        return Arrays.stream(EChatFlags.values()).filter(x -> (x.code & code) == x.code)
                .collect(Collectors.toCollection(() -> EnumSet.noneOf(EChatFlags.class)));
    }

    public static int code(EnumSet<EChatFlags> flags) {
        return flags.stream().map(flag -> flag.code).reduce(0, (a, b) -> a | b);
    }
}
