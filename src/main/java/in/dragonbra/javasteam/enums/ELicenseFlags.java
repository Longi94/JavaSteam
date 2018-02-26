package in.dragonbra.javasteam.enums;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.stream.Collectors;

public enum ELicenseFlags {

    None(0),
    Renew(0x01),
    RenewalFailed(0x02),
    Pending(0x04),
    Expired(0x08),
    CancelledByUser(0x10),
    CancelledByAdmin(0x20),
    LowViolenceContent(0x40),
    ImportedFromSteam2(0x80),
    ForceRunRestriction(0x100),
    RegionRestrictionExpired(0x200),
    CancelledByFriendlyFraudLock(0x400),
    NotActivated(0x800),

    ;

    private final int code;

    ELicenseFlags(int code) {
        this.code = code;
    }

    public int code() {
        return this.code;
    }

    public static EnumSet<ELicenseFlags> from(int code) {
        return Arrays.stream(ELicenseFlags.values()).filter(x -> (x.code & code) == x.code)
                .collect(Collectors.toCollection(() -> EnumSet.noneOf(ELicenseFlags.class)));
    }

    public static int code(EnumSet<ELicenseFlags> flags) {
        return flags.stream().map(flag -> flag.code).reduce(0, (a, b) -> a | b);
    }
}
