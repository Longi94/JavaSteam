package in.dragonbra.javasteam.enums;

import java.util.Arrays;

public enum EChatRoomType {

    Friend(1),
    MUC(2),
    Lobby(3),

    ;

    private final int code;

    EChatRoomType(int code) {
        this.code = code;
    }

    public int code() {
        return this.code;
    }

    public static EChatRoomType from(int code) {
        return Arrays.stream(EChatRoomType.values()).filter(x -> x.code == code).findFirst().orElse(null);
    }
}
