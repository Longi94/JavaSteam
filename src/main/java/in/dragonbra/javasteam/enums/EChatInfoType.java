package in.dragonbra.javasteam.enums;

import java.util.Arrays;

public enum EChatInfoType {

    StateChange(1),
    InfoUpdate(2),
    MemberLimitChange(3),

    ;

    private final int code;

    EChatInfoType(int code) {
        this.code = code;
    }

    public int code() {
        return this.code;
    }

    public static EChatInfoType from(int code) {
        return Arrays.stream(EChatInfoType.values()).filter(x -> x.code == code).findFirst().orElse(null);
    }
}
