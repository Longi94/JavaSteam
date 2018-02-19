package in.dragonbra.javasteam.enums;

import java.util.Arrays;

public enum EDRMBlobDownloadErrorDetail {

    None(0),
    DownloadFailed(1),
    TargetLocked(2),
    OpenZip(3),
    ReadZipDirectory(4),
    UnexpectedZipEntry(5),
    UnzipFullFile(6),
    UnknownBlobType(7),
    UnzipStrips(8),
    UnzipMergeGuid(9),
    UnzipSignature(10),
    ApplyStrips(11),
    ApplyMergeGuid(12),
    ApplySignature(13),
    AppIdMismatch(14),
    AppIdUnexpected(15),
    AppliedSignatureCorrupt(16),
    ApplyValveSignatureHeader(17),
    UnzipValveSignatureHeader(18),
    PathManipulationError(19),
    TargetLocked_Base(65536),
    TargetLocked_Max(131071),
    NextBase(131072),

    ;

    private final int code;

    EDRMBlobDownloadErrorDetail(int code) {
        this.code = code;
    }

    public int getCode() {
        return this.code;
    }

    public EDRMBlobDownloadErrorDetail from(int code) {
        return Arrays.stream(EDRMBlobDownloadErrorDetail.values()).filter(x -> x.code == code).findFirst().orElse(null);
    }
}
