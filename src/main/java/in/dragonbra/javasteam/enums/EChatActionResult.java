package in.dragonbra.javasteam.enums;

import java.util.Arrays;

public enum EChatActionResult {

    Success(1),
    Error(2),
    NotPermitted(3),
    NotAllowedOnClanMember(4),
    NotAllowedOnBannedUser(5),
    NotAllowedOnChatOwner(6),
    NotAllowedOnSelf(7),
    ChatDoesntExist(8),
    ChatFull(9),
    VoiceSlotsFull(10),

    ;

    private final int code;

    EChatActionResult(int code) {
        this.code = code;
    }

    public int code() {
        return this.code;
    }

    public static EChatActionResult from(int code) {
        return Arrays.stream(EChatActionResult.values()).filter(x -> x.code == code).findFirst().orElse(null);
    }
}
