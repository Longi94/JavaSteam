package in.dragonbra.javasteam.enums;

import java.util.Arrays;

public enum EAuthSessionResponse {

    OK(0),
    UserNotConnectedToSteam(1),
    NoLicenseOrExpired(2),
    VACBanned(3),
    LoggedInElseWhere(4),
    VACCheckTimedOut(5),
    AuthTicketCanceled(6),
    AuthTicketInvalidAlreadyUsed(7),
    AuthTicketInvalid(8),
    PublisherIssuedBan(9),

    ;

    private final int code;

    EAuthSessionResponse(int code) {
        this.code = code;
    }

    public int code() {
        return this.code;
    }

    public static EAuthSessionResponse from(int code) {
        return Arrays.stream(EAuthSessionResponse.values()).filter(x -> x.code == code).findFirst().orElse(null);
    }
}
