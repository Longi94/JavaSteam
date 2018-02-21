package in.dragonbra.javasteam.generated;

import in.dragonbra.javasteam.base.ISteamSerializableHeader;
import in.dragonbra.javasteam.enums.EMsg;
import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesBase.CMsgProtoBufHeader;

import java.io.InputStream;
import java.io.OutputStream;

public class MsgHdrProtoBuf implements ISteamSerializableHeader {

    private EMsg msg = EMsg.Invalid;

    private int headerLength = 0;

    private CMsgProtoBufHeader proto = CMsgProtoBufHeader.newBuilder().build();

    @Override
    public void setEMsg(EMsg msg) {
        this.msg = msg;
    }

    public EMsg getMsg() {
        return this.msg;
    }

    public void setMsg(EMsg msg) {
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
