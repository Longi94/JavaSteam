package in.dragonbra.javasteam.enums;

import java.util.Arrays;

public enum EPersonaState {

    Offline(0),
    Online(1),
    Busy(2),
    Away(3),
    Snooze(4),
    LookingToTrade(5),
    LookingToPlay(6),
    Max(7),

    ;

    private final int code;

    EPersonaState(int code) {
        this.code = code;
    }

    public int getCode() {
        return this.code;
    }

    public EPersonaState from(int code) {
        return Arrays.stream(EPersonaState.values()).filter(x -> x.code == code).findFirst().orElse(null);
    }
}
