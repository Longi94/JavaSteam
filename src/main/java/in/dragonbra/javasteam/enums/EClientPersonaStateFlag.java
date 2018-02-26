package in.dragonbra.javasteam.enums;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.stream.Collectors;

public enum EClientPersonaStateFlag {

    Status(1),
    PlayerName(2),
    QueryPort(4),
    SourceID(8),
    Presence(16),
    LastSeen(64),
    ClanInfo(128),
    GameExtraInfo(256),
    GameDataBlob(512),
    ClanTag(1024),
    Facebook(2048),

    ;

    private final int code;

    EClientPersonaStateFlag(int code) {
        this.code = code;
    }

    public int code() {
        return this.code;
    }

    public static EnumSet<EClientPersonaStateFlag> from(int code) {
        return Arrays.stream(EClientPersonaStateFlag.values()).filter(x -> (x.code & code) == x.code)
                .collect(Collectors.toCollection(() -> EnumSet.noneOf(EClientPersonaStateFlag.class)));
    }

    public static int code(EnumSet<EClientPersonaStateFlag> flags) {
        return flags.stream().map(flag -> flag.code).reduce(0, (a, b) -> a | b);
    }
}
