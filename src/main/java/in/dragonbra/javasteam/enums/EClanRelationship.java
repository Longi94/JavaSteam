package in.dragonbra.javasteam.enums;

import java.util.Arrays;

public enum EClanRelationship {

    None(0),
    Blocked(1),
    Invited(2),
    Member(3),
    Kicked(4),
    KickAcknowledged(5),

    ;

    private final int code;

    EClanRelationship(int code) {
        this.code = code;
    }

    public int getCode() {
        return this.code;
    }

    public EClanRelationship from(int code) {
        return Arrays.stream(EClanRelationship.values()).filter(x -> x.code == code).findFirst().orElse(null);
    }
}
