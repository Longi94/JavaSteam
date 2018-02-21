package in.dragonbra.javasteam.generated;

import in.dragonbra.javasteam.base.IGCSerializableHeader;

import java.io.InputStream;
import java.io.OutputStream;

public class MsgGCHdr implements IGCSerializableHeader {

    private int headerVersion = 1;

    private long targetJobID = Long.MAX_VALUE;

    private long sourceJobID = Long.MAX_VALUE;

    @Override
    public void setEMsg(int msg) {}

    public int getHeaderVersion() {
        return this.headerVersion;
    }

    public void setHeaderVersion(int headerVersion) {
        this.headerVersion = headerVersion;
    }

    public long getTargetJobID() {
        return this.targetJobID;
    }

    public void setTargetJobID(long targetJobID) {
        this.targetJobID = targetJobID;
    }

    public long getSourceJobID() {
        return this.sourceJobID;
    }

    public void setSourceJobID(long sourceJobID) {
        this.sourceJobID = sourceJobID;
    }

    @Override
    public void serialize(OutputStream stream) {
    }

    @Override
    public void deserialize(InputStream stream) {
    }
}
