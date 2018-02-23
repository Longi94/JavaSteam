package in.dragonbra.javasteam.generated;

import in.dragonbra.javasteam.base.ISteamSerializableMessage;
import in.dragonbra.javasteam.enums.EMsg;
import in.dragonbra.javasteam.util.stream.BinaryReader;

import java.io.*;

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
    public void serialize(OutputStream stream) throws IOException {
        DataOutputStream dos = new DataOutputStream(stream);

        dos.writeLong(uniqueID);
        dos.writeInt(loginKey.length);
        dos.write(loginKey);
    }

    @Override
    public void deserialize(InputStream stream) throws IOException {
        BinaryReader br = new BinaryReader(stream);

        uniqueID = br.readLong();
        loginKey = br.readBytes(br.readInt());
    }
}
