package in.dragonbra.javasteam.enums;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.stream.Collectors;

public enum EClanPermission {

    Nobody(0),
    Owner(1),
    Officer(2),
    OwnerAndOfficer(3),
    Member(4),
    Moderator(8),
    OwnerOfficerModerator(Owner.code | Officer.code | Moderator.code),
    AllMembers(Owner.code | Officer.code | Moderator.code | Member.code),
    OGGGameOwner(16),
    NonMember(128),
    MemberAllowed(NonMember.code | Member.code),
    ModeratorAllowed(NonMember.code | Member.code | Moderator.code),
    OfficerAllowed(NonMember.code | Member.code | Moderator.code | Officer.code),
    OwnerAllowed(NonMember.code | Member.code | Moderator.code | Officer.code | Owner.code),
    Anybody(NonMember.code | Member.code | Moderator.code | Officer.code | Owner.code),

    ;

    private final int code;

    EClanPermission(int code) {
        this.code = code;
    }

    public int code() {
        return this.code;
    }

    public static EnumSet<EClanPermission> from(int code) {
        return Arrays.stream(EClanPermission.values()).filter(x -> (x.code & code) == x.code)
                .collect(Collectors.toCollection(() -> EnumSet.noneOf(EClanPermission.class)));
    }

    public static int code(EnumSet<EClanPermission> flags) {
        return flags.stream().map(flag -> flag.code).reduce(0, (a, b) -> a | b);
    }
}
