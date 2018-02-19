package in.dragonbra.javasteam.enums;

import java.util.Arrays;

public enum EAccountType {

    Invalid(0),
    Individual(1),
    Multiseat(2),
    GameServer(3),
    AnonGameServer(4),
    Pending(5),
    ContentServer(6),
    Clan(7),
    Chat(8),
    ConsoleUser(9),
    AnonUser(10),
    Max(11),

    ;

    private final int code;

    EAccountType(int code) {
        this.code = code;
    }

    public int getCode() {
        return this.code;
    }

    public EAccountType from(int code) {
        return Arrays.stream(EAccountType.values()).filter(x -> x.code == code).findFirst().orElse(null);
    }
}
