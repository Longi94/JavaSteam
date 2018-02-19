package in.dragonbra.javasteam.enums;

import java.util.Arrays;

public enum EWorkshopFileType {

    First(0),
    Community(0),
    Microtransaction(1),
    Collection(2),
    Art(3),
    Video(4),
    Screenshot(5),
    Game(6),
    Software(7),
    Concept(8),
    WebGuide(9),
    IntegratedGuide(10),
    Merch(11),
    ControllerBinding(12),
    SteamworksAccessInvite(13),
    SteamVideo(14),
    GameManagedItem(15),
    Max(16),

    ;

    private final int code;

    EWorkshopFileType(int code) {
        this.code = code;
    }

    public int getCode() {
        return this.code;
    }

    public EWorkshopFileType from(int code) {
        return Arrays.stream(EWorkshopFileType.values()).filter(x -> x.code == code).findFirst().orElse(null);
    }
}
