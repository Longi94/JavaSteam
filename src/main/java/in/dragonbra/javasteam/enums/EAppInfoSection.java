package in.dragonbra.javasteam.enums;

import java.util.Arrays;

public enum EAppInfoSection {

    Unknown(0),
    All(1),
    First(2),
    Common(2),
    Extended(3),
    Config(4),
    Stats(5),
    Install(6),
    Depots(7),
    UFS(10),
    OGG(11),
    Policies(13),
    SysReqs(14),
    Community(15),
    Store(16),
    Max(17),

    ;

    private final int code;

    EAppInfoSection(int code) {
        this.code = code;
    }

    public int code() {
        return this.code;
    }

    public static EAppInfoSection from(int code) {
        return Arrays.stream(EAppInfoSection.values()).filter(x -> x.code == code).findFirst().orElse(null);
    }
}
