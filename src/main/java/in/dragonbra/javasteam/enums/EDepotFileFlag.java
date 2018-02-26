package in.dragonbra.javasteam.enums;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.stream.Collectors;

public enum EDepotFileFlag {

    UserConfig(1),
    VersionedUserConfig(2),
    Encrypted(4),
    ReadOnly(8),
    Hidden(16),
    Executable(32),
    Directory(64),
    CustomExecutable(128),
    InstallScript(256),
    Symlink(512),

    ;

    private final int code;

    EDepotFileFlag(int code) {
        this.code = code;
    }

    public int code() {
        return this.code;
    }

    public static EnumSet<EDepotFileFlag> from(int code) {
        return Arrays.stream(EDepotFileFlag.values()).filter(x -> (x.code & code) == x.code)
                .collect(Collectors.toCollection(() -> EnumSet.noneOf(EDepotFileFlag.class)));
    }

    public static int code(EnumSet<EDepotFileFlag> flags) {
        return flags.stream().map(flag -> flag.code).reduce(0, (a, b) -> a | b);
    }
}
