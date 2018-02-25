package in.dragonbra.javasteam.generated;

import in.dragonbra.javasteam.base.IGCSerializableHeader;
import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesBase.CMsgProtoBufHeader;
import in.dragonbra.javasteam.util.stream.BinaryReader;
import in.dragonbra.javasteam.util.stream.BinaryWriter;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class MsgGCHdrProtoBuf implements IGCSerializableHeader {

    private int msg = 0;

    private int headerLength = 0;

    private CMsgProtoBufHeader.Builder proto = CMsgProtoBufHeader.newBuilder();

    @Override
    public void setEMsg(int msg) {
        this.msg = msg;
    }

    public int getMsg() {
        return this.msg;
    }

    public void setMsg(int msg) {
        this.msg = msg;
    }

    public int getHeaderLength() {
        return this.headerLength;
    }

    public void setHeaderLength(int headerLength) {
        this.headerLength = headerLength;
    }

    public CMsgProtoBufHeader.Builder getProto() {
        return this.proto;
    }

    public void setProto(CMsgProtoBufHeader.Builder proto) {
        this.proto = proto;
    }

    @Override
    public void serialize(OutputStream stream) throws IOException {
        BinaryWriter bw = new BinaryWriter(stream);

        bw.writeInt(msg);
        byte[] protoBuffer = proto.build().toByteArray();
        headerLength = protoBuffer.length;
        bw.writeInt(headerLength);
        bw.write(protoBuffer);
    }

    @Override
    public void deserialize(InputStream stream) throws IOException {
        BinaryReader br = new BinaryReader(stream);

        msg = br.readInt();
        headerLength = br.readInt();
        byte[] protoBuffer = br.readBytes(headerLength);
        proto = CMsgProtoBufHeader.newBuilder().mergeFrom(protoBuffer);
    }
}
