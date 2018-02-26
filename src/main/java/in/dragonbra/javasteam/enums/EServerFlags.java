package in.dragonbra.javasteam.enums;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.stream.Collectors;

public enum EServerFlags {

    None(0),
    Active(1),
    Secure(2),
    Dedicated(4),
    Linux(8),
    Passworded(16),
    Private(32),

    ;

    private final int code;

    EServerFlags(int code) {
        this.code = code;
    }

    public int code() {
        return this.code;
    }

    public static EnumSet<EServerFlags> from(int code) {
        return Arrays.stream(EServerFlags.values()).filter(x -> (x.code & code) == x.code)
                .collect(Collectors.toCollection(() -> EnumSet.noneOf(EServerFlags.class)));
    }

    public static int code(EnumSet<EServerFlags> flags) {
        return flags.stream().map(flag -> flag.code).reduce(0, (a, b) -> a | b);
    }
}
