package in.dragonbra.javasteam.enums;

import java.util.Arrays;

public enum EWorkshopEnumerationType {

    RankedByVote(0),
    Recent(1),
    Trending(2),
    FavoriteOfFriends(3),
    VotedByFriends(4),
    ContentByFriends(5),
    RecentFromFollowedUsers(6),

    ;

    private final int code;

    EWorkshopEnumerationType(int code) {
        this.code = code;
    }

    public int code() {
        return this.code;
    }

    public static EWorkshopEnumerationType from(int code) {
        return Arrays.stream(EWorkshopEnumerationType.values()).filter(x -> x.code == code).findFirst().orElse(null);
    }
}
