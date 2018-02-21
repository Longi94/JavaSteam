package in.dragonbra.javasteam.generated;

import in.dragonbra.javasteam.base.ISteamSerializableMessage;
import in.dragonbra.javasteam.enums.EMsg;

import java.io.InputStream;
import java.io.OutputStream;

public class MsgClientNewLoginKey implements ISteamSerializableMessage {

    private long uniqueID = 0L;

    private byte[] loginKey = new byte[20];

    @Override
    public EMsg getEMsg() {
        return EMsg.ClientNewLoginKey;
    }

    public long getUniqueID() {
        return this.uniqueID;
    }

    public void setUniqueID(long uniqueID) {
        this.uniqueID = uniqueID;
    }

    public byte[] getLoginKey() {
        return this.loginKey;
    }

    public void setLoginKey(byte[] loginKey) {
        this.loginKey = loginKey;
    }

    @Override
    public void serialize(OutputStream stream) {
    }

    @Override
    public void deserialize(InputStream stream) {
    }
}
