package in.dragonbra.javasteam.enums;

import java.util.Arrays;

public enum EChatRoomEnterResponse {

    Success(1),
    DoesntExist(2),
    NotAllowed(3),
    Full(4),
    Error(5),
    Banned(6),
    Limited(7),
    ClanDisabled(8),
    CommunityBan(9),
    MemberBlockedYou(10),
    YouBlockedMember(11),

    ;

    private final int code;

    EChatRoomEnterResponse(int code) {
        this.code = code;
    }

    public int getCode() {
        return this.code;
    }

    public EChatRoomEnterResponse from(int code) {
        return Arrays.stream(EChatRoomEnterResponse.values()).filter(x -> x.code == code).findFirst().orElse(null);
    }
}
