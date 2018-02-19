package in.dragonbra.javasteam.enums;

import java.util.Arrays;

public enum EFriendRelationship {

    None(0),
    Blocked(1),
    RequestRecipient(2),
    Friend(3),
    RequestInitiator(4),
    Ignored(5),
    IgnoredFriend(6),
    SuggestedFriend(7),
    Max(8),

    ;

    private final int code;

    EFriendRelationship(int code) {
        this.code = code;
    }

    public int getCode() {
        return this.code;
    }

    public EFriendRelationship from(int code) {
        return Arrays.stream(EFriendRelationship.values()).filter(x -> x.code == code).findFirst().orElse(null);
    }
}
