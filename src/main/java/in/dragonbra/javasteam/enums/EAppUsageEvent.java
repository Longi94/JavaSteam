package in.dragonbra.javasteam.enums;

import java.util.Arrays;

public enum EAppUsageEvent {

    GameLaunch(1),
    GameLaunchTrial(2),
    Media(3),
    PreloadStart(4),
    PreloadFinish(5),
    MarketingMessageView(6),
    InGameAdViewed(7),
    GameLaunchFreeWeekend(8),

    ;

    private final int code;

    EAppUsageEvent(int code) {
        this.code = code;
    }

    public int code() {
        return this.code;
    }

    public static EAppUsageEvent from(int code) {
        return Arrays.stream(EAppUsageEvent.values()).filter(x -> x.code == code).findFirst().orElse(null);
    }
}
