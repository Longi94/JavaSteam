package in.dragonbra.javasteam.enums;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.stream.Collectors;

public enum EFriendFlags {

    None(0),
    Blocked(1),
    FriendshipRequested(2),
    Immediate(4),
    ClanMember(8),
    OnGameServer(16),
    RequestingFriendship(128),
    RequestingInfo(256),
    Ignored(512),
    IgnoredFriend(1024),
    Suggested(2048),
    ChatMember(4096),
    FlagAll(65535),

    ;

    private final int code;

    EFriendFlags(int code) {
        this.code = code;
    }

    public int code() {
        return this.code;
    }

    public static EnumSet<EFriendFlags> from(int code) {
        return Arrays.stream(EFriendFlags.values()).filter(x -> (x.code & code) == x.code)
                .collect(Collectors.toCollection(() -> EnumSet.noneOf(EFriendFlags.class)));
    }

    public static int code(EnumSet<EFriendFlags> flags) {
        return flags.stream().map(flag -> flag.code).reduce(0, (a, b) -> a | b);
    }
}
