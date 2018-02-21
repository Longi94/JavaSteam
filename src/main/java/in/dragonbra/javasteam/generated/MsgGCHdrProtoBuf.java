package in.dragonbra.javasteam.generated;

import in.dragonbra.javasteam.base.IGCSerializableHeader;
import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesBase.CMsgProtoBufHeader;

import java.io.InputStream;
import java.io.OutputStream;

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
    public void serialize(OutputStream stream) {
    }

    @Override
    public void deserialize(InputStream stream) {
    }
}
