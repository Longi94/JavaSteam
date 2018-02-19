package in.dragonbra.javasteam.enums;

import java.util.Arrays;

public enum EContentDownloadSourceType {

    Invalid(0),
    CS(1),
    CDN(2),
    LCS(3),
    ProxyCache(4),
    LANPeer(5),
    Max(5),

    ;

    private final int code;

    EContentDownloadSourceType(int code) {
        this.code = code;
    }

    public int getCode() {
        return this.code;
    }

    public EContentDownloadSourceType from(int code) {
        return Arrays.stream(EContentDownloadSourceType.values()).filter(x -> x.code == code).findFirst().orElse(null);
    }
}
