package in.dragonbra.javasteam.enums;

import java.util.Arrays;

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

    public int getCode() {
        return this.code;
    }

    public EClanPermission from(int code) {
        return Arrays.stream(EClanPermission.values()).filter(x -> x.code == code).findFirst().orElse(null);
    }
}
