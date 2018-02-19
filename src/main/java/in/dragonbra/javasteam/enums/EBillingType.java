package in.dragonbra.javasteam.enums;

import java.util.Arrays;

public enum EBillingType {

    NoCost(0),
    BillOnceOnly(1),
    BillMonthly(2),
    ProofOfPrepurchaseOnly(3),
    GuestPass(4),
    HardwarePromo(5),
    Gift(6),
    AutoGrant(7),
    OEMTicket(8),
    RecurringOption(9),
    BillOnceOrCDKey(10),
    Repurchaseable(11),
    FreeOnDemand(12),
    Rental(13),
    CommercialLicense(14),
    FreeCommercialLicense(15),
    NumBillingTypes(16),

    ;

    private final int code;

    EBillingType(int code) {
        this.code = code;
    }

    public int getCode() {
        return this.code;
    }

    public EBillingType from(int code) {
        return Arrays.stream(EBillingType.values()).filter(x -> x.code == code).findFirst().orElse(null);
    }
}
