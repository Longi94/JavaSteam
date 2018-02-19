package in.dragonbra.javasteam.enums;

import java.util.Arrays;

public enum EDenyReason {

    InvalidVersion(1),
    Generic(2),
    NotLoggedOn(3),
    NoLicense(4),
    Cheater(5),
    LoggedInElseWhere(6),
    UnknownText(7),
    IncompatibleAnticheat(8),
    MemoryCorruption(9),
    IncompatibleSoftware(10),
    SteamConnectionLost(11),
    SteamConnectionError(12),
    SteamResponseTimedOut(13),
    SteamValidationStalled(14),
    SteamOwnerLeftGuestUser(15),

    ;

    private final int code;

    EDenyReason(int code) {
        this.code = code;
    }

    public int code() {
        return this.code;
    }

    public static EDenyReason from(int code) {
        return Arrays.stream(EDenyReason.values()).filter(x -> x.code == code).findFirst().orElse(null);
    }
}
