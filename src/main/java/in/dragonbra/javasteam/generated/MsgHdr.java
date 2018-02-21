package in.dragonbra.javasteam.generated;

import in.dragonbra.javasteam.base.ISteamSerializableHeader;
import in.dragonbra.javasteam.enums.EMsg;

import java.io.InputStream;
import java.io.OutputStream;

public class MsgHdr implements ISteamSerializableHeader {

    private EMsg msg = EMsg.Invalid;

    private long targetJobID = Long.MAX_VALUE;

    private long sourceJobID = Long.MAX_VALUE;

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
