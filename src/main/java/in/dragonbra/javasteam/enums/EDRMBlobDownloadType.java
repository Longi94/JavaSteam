package in.dragonbra.javasteam.enums;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.stream.Collectors;

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

    public static EnumSet<EDRMBlobDownloadType> from(int code) {
        return Arrays.stream(EDRMBlobDownloadType.values()).filter(x -> (x.code & code) == x.code)
                .collect(Collectors.toCollection(() -> EnumSet.noneOf(EDRMBlobDownloadType.class)));
    }

    public static int code(EnumSet<EDRMBlobDownloadType> flags) {
        return flags.stream().map(flag -> flag.code).reduce(0, (a, b) -> a | b);
    }
}
