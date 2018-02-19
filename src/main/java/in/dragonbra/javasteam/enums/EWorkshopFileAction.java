package in.dragonbra.javasteam.enums;

import java.util.Arrays;

public enum EWorkshopFileAction {

    Played(0),
    Completed(1),

    ;

    private final int code;

    EWorkshopFileAction(int code) {
        this.code = code;
    }

    public int code() {
        return this.code;
    }

    public static EWorkshopFileAction from(int code) {
        return Arrays.stream(EWorkshopFileAction.values()).filter(x -> x.code == code).findFirst().orElse(null);
    }
}
