package in.dragonbra.javasteam.enums;

import java.util.Arrays;

public enum EEconTradeResponse {

    Accepted(0),
    Declined(1),
    TradeBannedInitiator(2),
    TradeBannedTarget(3),
    TargetAlreadyTrading(4),
    Disabled(5),
    NotLoggedIn(6),
    Cancel(7),
    TooSoon(8),
    TooSoonPenalty(9),
    ConnectionFailed(10),
    AlreadyTrading(11),
    AlreadyHasTradeRequest(12),
    NoResponse(13),
    CyberCafeInitiator(14),
    CyberCafeTarget(15),
    SchoolLabInitiator(16),
    SchoolLabTarget(16),
    InitiatorBlockedTarget(18),
    InitiatorNeedsVerifiedEmail(20),
    InitiatorNeedsSteamGuard(21),
    TargetAccountCannotTrade(22),
    InitiatorSteamGuardDuration(23),
    InitiatorPasswordResetProbation(24),
    InitiatorNewDeviceCooldown(25),
    InitiatorSentInvalidCookie(26),
    NeedsEmailConfirmation(27),
    InitiatorRecentEmailChange(28),
    NeedsMobileConfirmation(29),
    TradingHoldForClearedTradeOffersInitiator(30),
    WouldExceedMaxAssetCount(31),
    OKToDeliver(50),

    ;

    private final int code;

    EEconTradeResponse(int code) {
        this.code = code;
    }

    public int getCode() {
        return this.code;
    }

    public EEconTradeResponse from(int code) {
        return Arrays.stream(EEconTradeResponse.values()).filter(x -> x.code == code).findFirst().orElse(null);
    }
}
