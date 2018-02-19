package in.dragonbra.javasteam.enums;

import java.util.Arrays;

public enum EChatAction {

    InviteChat(1),
    Kick(2),
    Ban(3),
    UnBan(4),
    StartVoiceSpeak(5),
    EndVoiceSpeak(6),
    LockChat(7),
    UnlockChat(8),
    CloseChat(9),
    SetJoinable(10),
    SetUnjoinable(11),
    SetOwner(12),
    SetInvisibleToFriends(13),
    SetVisibleToFriends(14),
    SetModerated(15),
    SetUnmoderated(16),

    ;

    private final int code;

    EChatAction(int code) {
        this.code = code;
    }

    public int getCode() {
        return this.code;
    }

    public EChatAction from(int code) {
        return Arrays.stream(EChatAction.values()).filter(x -> x.code == code).findFirst().orElse(null);
    }
}
