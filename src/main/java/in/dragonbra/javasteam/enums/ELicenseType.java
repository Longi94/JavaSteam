package in.dragonbra.javasteam.enums;

import java.util.Arrays;

public enum ELicenseType {

    NoLicense(0),
    SinglePurchase(1),
    SinglePurchaseLimitedUse(2),
    RecurringCharge(3),
    RecurringChargeLimitedUse(4),
    RecurringChargeLimitedUseWithOverages(5),
    RecurringOption(6),
    LimitedUseDelayedActivation(7),

    ;

    private final int code;

    ELicenseType(int code) {
        this.code = code;
    }

    public int code() {
        return this.code;
    }

    public static ELicenseType from(int code) {
        return Arrays.stream(ELicenseType.values()).filter(x -> x.code == code).findFirst().orElse(null);
    }
}
