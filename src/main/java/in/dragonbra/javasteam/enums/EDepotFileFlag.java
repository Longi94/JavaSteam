package in.dragonbra.javasteam.enums;

import java.util.Arrays;

public enum EDepotFileFlag {

    UserConfig(1),
    VersionedUserConfig(2),
    Encrypted(4),
    ReadOnly(8),
    Hidden(16),
    Executable(32),
    Directory(64),
    CustomExecutable(128),
    InstallScript(256),
    Symlink(512),

    ;

    private final int code;

    EDepotFileFlag(int code) {
        this.code = code;
    }

    public int code() {
        return this.code;
    }

    public static EDepotFileFlag from(int code) {
        return Arrays.stream(EDepotFileFlag.values()).filter(x -> x.code == code).findFirst().orElse(null);
    }
}
