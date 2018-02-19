package in.dragonbra.javasteam.enums;

import java.util.Arrays;

public enum EDRMBlobDownloadType {

    Error(0),
    File(1),
    Parts(2),
    Compressed(4),
    AllMask(7),
    IsJob(8),
    HighPriority(16),
    AddTimestamp(32),
    LowPriority(64),

    ;

    private final int code;

    EDRMBlobDownloadType(int code) {
        this.code = code;
    }

    public int code() {
        return this.code;
    }

    public static EDRMBlobDownloadType from(int code) {
        return Arrays.stream(EDRMBlobDownloadType.values()).filter(x -> x.code == code).findFirst().orElse(null);
    }
}
