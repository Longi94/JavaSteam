package in.dragonbra.javasteam.generated;

import in.dragonbra.javasteam.base.IGCSerializableHeader;
import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesBase.CMsgProtoBufHeader;

import java.io.*;

public class MsgGCHdrProtoBuf implements IGCSerializableHeader {

    private long msg = 0L;

    private int headerLength = 0;

    private CMsgProtoBufHeader proto = CMsgProtoBufHeader.newBuilder().build();

    @Override
    public void setEMsg(int msg) {
        this.msg = msg;
    }

    public long getMsg() {
        return this.msg;
    }

    public void setMsg(long msg) {
        this.msg = msg;
    }

    public int getHeaderLength() {
        return this.headerLength;
    }

    public void setHeaderLength(int headerLength) {
        this.headerLength = headerLength;
    }

    public CMsgProtoBufHeader getProto() {
        return this.proto;
    }

    public void setProto(CMsgProtoBufHeader proto) {
        this.proto = proto;
    }

    @Override
    public void serialize(OutputStream stream) throws IOException {
        DataOutputStream dos = new DataOutputStream(stream);

        dos.writeLong(msg);
        dos.writeInt(headerLength);
        byte[] protoBuffer = proto.toByteArray();
        dos.writeInt(protoBuffer.length);
        dos.write(protoBuffer);
    }

    @Override
    public void deserialize(InputStream stream) throws IOException {
        DataInputStream dis = new DataInputStream(stream);

        msg = dis.readLong();
        headerLength = dis.readInt();
        byte[] protoBuffer = new byte[dis.readInt()];
        dis.readFully(protoBuffer);
        proto = CMsgProtoBufHeader.newBuilder().mergeFrom(protoBuffer).build();
    }
}
