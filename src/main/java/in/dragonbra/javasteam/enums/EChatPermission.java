package in.dragonbra.javasteam.enums;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.stream.Collectors;

public enum EChatPermission {

    Close(1),
    Invite(2),
    Talk(8),
    Kick(16),
    Mute(32),
    SetMetadata(64),
    ChangePermissions(128),
    Ban(256),
    ChangeAccess(512),
    Mask(1019),

    ;

    public static final EnumSet<EChatPermission> EveryoneNotInClanDefault = EnumSet.of(Talk);

    public static final EnumSet<EChatPermission> EveryoneDefault = EnumSet.of(Talk, Invite);

    public static final EnumSet<EChatPermission> MemberDefault = EnumSet.of(Ban, Kick, Talk, Invite);

    public static final EnumSet<EChatPermission> OfficerDefault = EnumSet.of(Ban, Kick, Talk, Invite);

    public static final EnumSet<EChatPermission> OwnerDefault = EnumSet.of(ChangeAccess, Ban, SetMetadata, Mute, Kick, Talk, Invite, Close);

    private final int code;

    EChatPermission(int code) {
        this.code = code;
    }

    public int code() {
        return this.code;
    }

    public static EnumSet<EChatPermission> from(int code) {
        return Arrays.stream(EChatPermission.values()).filter(x -> (x.code & code) == x.code)
                .collect(Collectors.toCollection(() -> EnumSet.noneOf(EChatPermission.class)));
    }

    public static int code(EnumSet<EChatPermission> flags) {
        return flags.stream().map(flag -> flag.code).reduce(0, (a, b) -> a | b);
    }
}
