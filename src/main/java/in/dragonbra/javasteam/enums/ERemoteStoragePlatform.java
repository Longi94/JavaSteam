package in.dragonbra.javasteam.enums;

import java.util.Arrays;

public enum ERemoteStoragePlatform {

    None(0),
    Windows(1),
    OSX(2),
    PS3(4),
    Linux(8),
    Reserved2(16),
    All(-1),

    ;

    private final int code;

    ERemoteStoragePlatform(int code) {
        this.code = code;
    }

    public int getCode() {
        return this.code;
    }

    public ERemoteStoragePlatform from(int code) {
        return Arrays.stream(ERemoteStoragePlatform.values()).filter(x -> x.code == code).findFirst().orElse(null);
    }
}
