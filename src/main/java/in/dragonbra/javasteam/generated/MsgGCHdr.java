package in.dragonbra.javasteam.generated;

import in.dragonbra.javasteam.base.IGCSerializableHeader;
import in.dragonbra.javasteam.util.stream.BinaryReader;

import java.io.*;

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
    public void serialize(OutputStream stream) throws IOException {
        DataOutputStream dos = new DataOutputStream(stream);

        dos.writeInt(headerVersion);
        dos.writeLong(targetJobID);
        dos.writeLong(sourceJobID);
    }

    @Override
    public void deserialize(InputStream stream) throws IOException {
        BinaryReader br = new BinaryReader(stream);

        headerVersion = br.readInt();
        targetJobID = br.readLong();
        sourceJobID = br.readLong();
    }
}
