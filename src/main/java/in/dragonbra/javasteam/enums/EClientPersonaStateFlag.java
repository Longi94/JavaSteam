package in.dragonbra.javasteam.enums;

import java.util.Arrays;

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

    public int getCode() {
        return this.code;
    }

    public EClientPersonaStateFlag from(int code) {
        return Arrays.stream(EClientPersonaStateFlag.values()).filter(x -> x.code == code).findFirst().orElse(null);
    }
}
